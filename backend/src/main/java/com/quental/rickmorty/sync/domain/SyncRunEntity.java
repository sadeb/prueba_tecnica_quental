package com.quental.rickmorty.sync.domain;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sync_runs")
public class SyncRunEntity {

    @Id
    @Column(length = 36)
    private String id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SyncRunStatus status;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "pages_fetched", nullable = false)
    private int pagesFetched;
    @Column(name = "messages_queued", nullable = false)
    private int messagesQueued;
    @Column(name = "processed_count", nullable = false)
    private int processedCount;
    @Column(name = "failed_count", nullable = false)
    private int failedCount;
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    protected SyncRunEntity() {
    }

    public static SyncRunEntity start() {
        SyncRunEntity entity = new SyncRunEntity();
        entity.id = UUID.randomUUID().toString();
        entity.status = SyncRunStatus.QUEUING;
        entity.startedAt = Instant.now();
        return entity;
    }

    public void pageFetched() { pagesFetched++; }
    public void messageQueued() { messagesQueued++; }
    public void queued() {
        status = SyncRunStatus.QUEUED;
        completeIfFinished();
    }
    public void processed() { processedCount++; completeIfFinished(); }
    public void failedMessage() { failedCount++; completeIfFinished(); }
    public void fail(String message) {
        status = SyncRunStatus.FAILED;
        errorMessage = truncate(message);
        completedAt = Instant.now();
    }
    private void completeIfFinished() {
        if (status != SyncRunStatus.FAILED && processedCount + failedCount >= messagesQueued && status != SyncRunStatus.QUEUING) {
            status = failedCount == 0 ? SyncRunStatus.COMPLETED : SyncRunStatus.COMPLETED_WITH_ERRORS;
            completedAt = Instant.now();
        }
    }
    private String truncate(String value) {
        return value == null || value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    public String getId() { return id; }
    public SyncRunStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public int getPagesFetched() { return pagesFetched; }
    public int getMessagesQueued() { return messagesQueued; }
    public int getProcessedCount() { return processedCount; }
    public int getFailedCount() { return failedCount; }
    public String getErrorMessage() { return errorMessage; }
}
