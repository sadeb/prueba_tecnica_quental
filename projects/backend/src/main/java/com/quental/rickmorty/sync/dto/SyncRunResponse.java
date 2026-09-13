package com.quental.rickmorty.sync.dto;

import com.quental.rickmorty.sync.SyncRun;
import com.quental.rickmorty.sync.SyncRunStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "SyncRun", description = "State and counters of a synchronisation run")
public final class SyncRunResponse {

    private final Long runId;
    private final SyncRunStatus status;
    private final Instant startedAt;
    private final Instant finishedAt;
    @Schema(description = "Messages published to Kafka by the producer")
    private final long publishedMessages;
    @Schema(description = "Source elements rejected by validation and skipped")
    private final long skippedItems;
    @Schema(description = "Source pages that could not be fetched after retries")
    private final long failedPages;
    @Schema(description = "Messages sent to a dead-letter topic by the consumer")
    private final long failedMessages;

    public SyncRunResponse(Long runId, SyncRunStatus status, Instant startedAt, Instant finishedAt,
                           long publishedMessages, long skippedItems, long failedPages, long failedMessages) {
        this.runId = runId;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.publishedMessages = publishedMessages;
        this.skippedItems = skippedItems;
        this.failedPages = failedPages;
        this.failedMessages = failedMessages;
    }

    public static SyncRunResponse from(SyncRun run) {
        return new SyncRunResponse(run.getId(), run.getStatus(), run.getStartedAt(), run.getFinishedAt(),
                run.getPublishedMessages(), run.getSkippedItems(), run.getFailedPages(), run.getFailedMessages());
    }

    public Long getRunId() {
        return runId;
    }

    public SyncRunStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
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
