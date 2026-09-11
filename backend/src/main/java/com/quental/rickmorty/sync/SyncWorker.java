package com.quental.rickmorty.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.quental.rickmorty.sync.domain.SyncRunEntity;
import com.quental.rickmorty.sync.external.ResourceType;
import com.quental.rickmorty.sync.external.RickMortyClient;
import com.quental.rickmorty.sync.external.dto.RickMortyPageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SyncWorker {

    private static final Logger logger = LoggerFactory.getLogger(SyncWorker.class);

    private final SyncRunRepository repository;
    private final RickMortyClient client;
    private final RawPayloadService rawPayloadService;
    private final TransactionTemplate transactions;

    public SyncWorker(SyncRunRepository repository, RickMortyClient client, RawPayloadService rawPayloadService,
                      TransactionTemplate transactions) {
        this.repository = repository;
        this.client = client;
        this.rawPayloadService = rawPayloadService;
        this.transactions = transactions;
    }

    @Async
    public void execute(String runId) {
        try {
            for (ResourceType type : new ResourceType[]{ResourceType.LOCATION, ResourceType.EPISODE, ResourceType.CHARACTER}) {
                fetchResource(runId, type);
            }
            markQueued(runId);
        } catch (RuntimeException exception) {
            logger.error("Synchronization {} failed while fetching external resources", runId, exception);
            markFailed(runId, exception.getMessage());
        }
    }

    private void fetchResource(String runId, ResourceType type) {
        int pageNumber = 1;
        int totalPages;
        do {
            RickMortyPageDto<JsonNode> page = client.fetchPage(type, pageNumber);
            totalPages = page.getInfo().getPages();
            queuePage(runId, type, page);
            pageNumber++;
        } while (pageNumber <= totalPages);
    }

    public void queuePage(String runId, ResourceType type, RickMortyPageDto<JsonNode> page) {
        transactions.executeWithoutResult(status -> {
            SyncRunEntity run = requiredRun(runId);
            run.pageFetched();
            for (JsonNode item : page.getResults()) {
                if (rawPayloadService.queue(run, type, item)) {
                    run.messageQueued();
                }
            }
            repository.save(run);
        });
    }

    public void markQueued(String runId) {
        transactions.executeWithoutResult(status -> {
            SyncRunEntity run = requiredRun(runId);
            run.queued();
            repository.save(run);
        });
    }

    public void markFailed(String runId, String message) {
        transactions.executeWithoutResult(status -> {
            SyncRunEntity run = requiredRun(runId);
            run.fail(message);
            repository.save(run);
        });
    }

    private SyncRunEntity requiredRun(String runId) {
        return repository.findById(runId).orElseThrow(() -> new IllegalStateException("Sync run disappeared: " + runId));
    }
}
