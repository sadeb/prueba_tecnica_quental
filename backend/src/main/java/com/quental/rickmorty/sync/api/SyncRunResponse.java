package com.quental.rickmorty.sync.api;

import com.quental.rickmorty.sync.domain.SyncRunEntity;
import java.time.Instant;

public class SyncRunResponse {
    private final String id;
    private final String status;
    private final Instant startedAt;
    private final Instant completedAt;
    private final int pagesFetched;
    private final int messagesQueued;
    private final int processedCount;
    private final int failedCount;
    private final String errorMessage;

    public SyncRunResponse(SyncRunEntity value) {
        this.id = value.getId(); this.status = value.getStatus().name(); this.startedAt = value.getStartedAt();
        this.completedAt = value.getCompletedAt(); this.pagesFetched = value.getPagesFetched();
        this.messagesQueued = value.getMessagesQueued(); this.processedCount = value.getProcessedCount();
        this.failedCount = value.getFailedCount(); this.errorMessage = value.getErrorMessage();
    }
    public String getId() { return id; }
    public String getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public int getPagesFetched() { return pagesFetched; }
    public int getMessagesQueued() { return messagesQueued; }
    public int getProcessedCount() { return processedCount; }
    public int getFailedCount() { return failedCount; }
    public String getErrorMessage() { return errorMessage; }
}
