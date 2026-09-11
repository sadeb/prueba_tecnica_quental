package com.quental.rickmorty.sync.domain;

public enum SyncRunStatus {
    QUEUING,
    QUEUED,
    COMPLETED,
    COMPLETED_WITH_ERRORS,
    FAILED
}
