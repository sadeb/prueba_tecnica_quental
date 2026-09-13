package com.quental.rickmorty.sync.producer;

import com.quental.rickmorty.common.ExternalServiceException;
import com.quental.rickmorty.common.InvalidExternalPayloadException;
import com.quental.rickmorty.external.ExternalCharacter;
import com.quental.rickmorty.external.ExternalEpisode;
import com.quental.rickmorty.external.ExternalLocation;
import com.quental.rickmorty.external.ExternalPage;
import com.quental.rickmorty.external.ExternalPageNotFoundException;
import com.quental.rickmorty.external.ExternalPayloadValidator;
import com.quental.rickmorty.external.ExternalSnapshotMapper;
import com.quental.rickmorty.external.RickAndMortyClient;
import com.quental.rickmorty.sync.SyncProperties;
import com.quental.rickmorty.sync.SyncRun;
import com.quental.rickmorty.sync.SyncRunService;
import com.quental.rickmorty.sync.SyncRunStatus;
import com.quental.rickmorty.sync.message.EntityType;
import com.quental.rickmorty.sync.message.SyncMessage;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Download + publish side of the sync (spec/03 points 1, 2, 4). Walks the paginated source in the
 * order locations -> episodes -> characters (ADR-002) and publishes one snapshot per element.
 * Failure policy: invalid element -> skipped (WARN, continue); page failure after the client's
 * retries -> failedPages (ERROR, next entity); run ends COMPLETED or PARTIAL, FAILED on unexpected error.
 */
@Service
public class SyncProducerService {

    private static final Logger log = LoggerFactory.getLogger(SyncProducerService.class);

    private final RickAndMortyClient client;
    private final ExternalPayloadValidator validator;
    private final ExternalSnapshotMapper mapper;
    private final SyncPublisher publisher;
    private final SyncMessageCodec codec;
    private final SyncRunService runService;
    private final SyncProperties properties;
    private final TaskExecutor taskExecutor;

    public SyncProducerService(RickAndMortyClient client,
                               ExternalPayloadValidator validator,
                               ExternalSnapshotMapper mapper,
                               SyncPublisher publisher,
                               SyncMessageCodec codec,
                               SyncRunService runService,
                               SyncProperties properties,
                               TaskExecutor taskExecutor) {
        this.client = client;
        this.validator = validator;
        this.mapper = mapper;
        this.publisher = publisher;
        this.codec = codec;
        this.runService = runService;
        this.properties = properties;
        this.taskExecutor = taskExecutor;
    }

    /** Creates the run (409 if one is running) and executes it on a worker thread; returns immediately. */
    public SyncRun launch() {
        SyncRun run = runService.start();
        taskExecutor.execute(() -> run(run.getId()));
        return run;
    }

    /** Synchronous execution of a run already created; public for tests and for the launcher. */
    public void run(Long runId) {
        Counters counters = new Counters();
        try {
            syncEntity(runId, "location", properties.getTopics().getLocations(), EntityType.LOCATION,
                    client::fetchLocations, this::toLocationMessage, counters);
            syncEntity(runId, "episode", properties.getTopics().getEpisodes(), EntityType.EPISODE,
                    client::fetchEpisodes, this::toEpisodeMessage, counters);
            syncEntity(runId, "character", properties.getTopics().getCharacters(), EntityType.CHARACTER,
                    client::fetchCharacters, this::toCharacterMessage, counters);
            SyncRunStatus status = counters.failedPages == 0 ? SyncRunStatus.COMPLETED : SyncRunStatus.PARTIAL;
            runService.finish(runId, status, counters.published, counters.skipped, counters.failedPages);
        } catch (RuntimeException ex) {
            log.error("sync run {} failed unexpectedly", runId, ex);
            try {
                runService.finish(runId, SyncRunStatus.FAILED, counters.published, counters.skipped, counters.failedPages);
            } catch (RuntimeException finishFailure) {
                // Database down: the row stays RUNNING until the next start (StaleSyncRunCleaner).
                log.error("sync run {} could not be marked FAILED: {}", runId, finishFailure.getMessage());
            }
        }
    }

    private <T> void syncEntity(Long runId, String resource, String topic, EntityType entityType,
                                IntFunction<ExternalPage<T>> fetcher,
                                Function<T, SnapshotMessage> toMessage, Counters counters) {
        int page = 1;
        while (true) {
            ExternalPage<T> result;
            try {
                result = fetcher.apply(page);
                validator.validatePage(result, resource, page);
            } catch (ExternalPageNotFoundException ex) {
                if (page == 1) {
                    log.error("sync run {}: {} page 1 not found, nothing to publish", runId, resource);
                    counters.failedPages++;
                }
                break;
            } catch (ExternalServiceException | InvalidExternalPayloadException ex) {
                log.error("sync run {}: {} page {} failed, moving to next entity: {}", runId, resource, page, ex.getMessage());
                counters.failedPages++;
                break;
            }
            int publishedInPage = 0;
            try {
                for (T element : result.getResults()) {
                    if (publishElement(runId, topic, entityType, element, toMessage, counters)) {
                        publishedInPage++;
                    }
                }
            } catch (SyncPublishException ex) {
                log.error("sync run {}: publishing {} page {} to {} failed, moving to next entity: {}",
                        runId, resource, page, topic, ex.getMessage());
                counters.failedPages++;
                break;
            }
            Integer totalPages = result.getInfo() == null ? null : result.getInfo().getPages();
            log.info("sync run {}: {} page {}/{} published={} (total so far {})",
                    runId, resource, page, totalPages == null ? "?" : totalPages, publishedInPage, counters.published);
            if (!result.hasNext()) {
                break;
            }
            page++;
        }
    }

    private <T> boolean publishElement(Long runId, String topic, EntityType entityType, T element,
                                       Function<T, SnapshotMessage> toMessage, Counters counters) {
        SnapshotMessage snapshot;
        try {
            snapshot = toMessage.apply(element);
        } catch (InvalidExternalPayloadException ex) {
            log.warn("sync run {}: skipping invalid {}: {}", runId, entityType, ex.getMessage());
            counters.skipped++;
            return false;
        }
        SyncMessage message = codec.build(runId, entityType, snapshot.externalId, snapshot.payload, Instant.now());
        publisher.publish(topic, message);
        counters.published++;
        return true;
    }

    private SnapshotMessage toLocationMessage(ExternalLocation source) {
        validator.validate(source);
        return new SnapshotMessage(source.getId(), mapper.toSnapshot(source));
    }

    private SnapshotMessage toEpisodeMessage(ExternalEpisode source) {
        validator.validate(source);
        return new SnapshotMessage(source.getId(), mapper.toSnapshot(source));
    }

    private SnapshotMessage toCharacterMessage(ExternalCharacter source) {
        validator.validate(source);
        return new SnapshotMessage(source.getId(), mapper.toSnapshot(source));
    }

    private static final class SnapshotMessage {
        private final long externalId;
        private final Object payload;

        private SnapshotMessage(long externalId, Object payload) {
            this.externalId = externalId;
            this.payload = payload;
        }
    }

    private static final class Counters {
        private long published;
        private long skipped;
        private long failedPages;
    }
}
