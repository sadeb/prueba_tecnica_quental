package com.quental.rickmorty.sync;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

/**
 * One explicit synchronisation with its outcome and counters (ADR-002; trace required by spec/03).
 * DynamicUpdate: finish() must not write back a stale failed_messages while the consumer thread
 * increments it with the atomic JPQL update.
 */
@Entity
@DynamicUpdate
@Table(name = "sync_runs")
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SyncRunStatus status;

    @Column(name = "published_messages", nullable = false)
    private long publishedMessages;

    @Column(name = "skipped_items", nullable = false)
    private long skippedItems;

    @Column(name = "failed_pages", nullable = false)
    private long failedPages;

    @Column(name = "failed_messages", nullable = false)
    private long failedMessages;

    protected SyncRun() {
    }

    public static SyncRun started(Instant now) {
        SyncRun run = new SyncRun();
        run.startedAt = now;
        run.status = SyncRunStatus.RUNNING;
        return run;
    }

    public void finish(SyncRunStatus finalStatus, long published, long skipped, long failedPageCount, Instant now) {
        this.status = finalStatus;
        this.publishedMessages = published;
        this.skippedItems = skipped;
        this.failedPages = failedPageCount;
        this.finishedAt = now;
    }

    public void markFailed(Instant now) {
        this.status = SyncRunStatus.FAILED;
        this.finishedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public SyncRunStatus getStatus() {
        return status;
    }

    public long getPublishedMessages() {
        return publishedMessages;
    }

    public long getSkippedItems() {
        return skippedItems;
    }

    public long getFailedPages() {
        return failedPages;
    }

    public long getFailedMessages() {
        return failedMessages;
    }
}
