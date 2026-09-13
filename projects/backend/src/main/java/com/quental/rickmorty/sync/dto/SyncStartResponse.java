package com.quental.rickmorty.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "SyncStart", description = "Accepted synchronisation run")
public final class SyncStartResponse {

    @Schema(example = "7")
    private final Long runId;
    private final Instant startedAt;

    public SyncStartResponse(Long runId, Instant startedAt) {
        this.runId = runId;
        this.startedAt = startedAt;
    }

    public Long getRunId() {
        return runId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }
}
