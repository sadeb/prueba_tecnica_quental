package com.quental.rickmorty.sync;

import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/** Lifecycle of sync_runs rows. Knows nothing about Kafka or the external source. */
@Service
public class SyncRunService {

    private static final Logger log = LoggerFactory.getLogger(SyncRunService.class);

    private final SyncRunJpaRepository repository;
    private final Clock clock;

    @Autowired
    public SyncRunService(SyncRunJpaRepository repository) {
        this(repository, Clock.systemUTC());
    }

    SyncRunService(SyncRunJpaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Creates a RUNNING row or fails with 409 if one exists. Synchronized (single instance) so two
     * concurrent POST /api/admin/sync cannot both pass the check; save() commits on its own so the
     * row is visible to the worker thread immediately.
     */
    public synchronized SyncRun start() {
        if (repository.existsByStatus(SyncRunStatus.RUNNING)) {
            throw new ConflictException("A synchronisation is already running");
        }
        SyncRun run = repository.save(SyncRun.started(Instant.now(clock)));
        log.info("sync run {} started", run.getId());
        return run;
    }

    @Transactional
    public void finish(Long runId, SyncRunStatus status, long published, long skipped, long failedPages) {
        SyncRun run = repository.findById(runId).orElseThrow(() -> NotFoundException.of("Sync run", runId));
        run.finish(status, published, skipped, failedPages, Instant.now(clock));
        log.info("sync run {} finished with status {}: published={} skipped={} failedPages={}",
                runId, status, published, skipped, failedPages);
    }

    /** Called by the DLT recoverer; falls back to the latest run when the payload carries no run id. */
    @Transactional
    public void recordFailedMessage(Optional<Long> runId) {
        Long target = runId.filter(repository::existsById)
                .or(() -> repository.findFirstByOrderByStartedAtDesc().map(SyncRun::getId))
                .orElse(null);
        if (target == null) {
            log.warn("Failed message could not be attributed to any sync run");
            return;
        }
        repository.incrementFailedMessages(target);
    }

    /** RUNNING rows left by a crashed process would block new runs forever (409). */
    @Transactional
    public int failStaleRuns() {
        int count = 0;
        for (SyncRun run : repository.findByStatus(SyncRunStatus.RUNNING)) {
            run.markFailed(Instant.now(clock));
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public SyncRun get(Long runId) {
        return repository.findById(runId).orElseThrow(() -> NotFoundException.of("Sync run", runId));
    }
}
