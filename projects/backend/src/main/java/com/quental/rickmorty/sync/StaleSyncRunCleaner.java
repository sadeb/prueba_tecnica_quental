package com.quental.rickmorty.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class StaleSyncRunCleaner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StaleSyncRunCleaner.class);

    private final SyncRunService syncRunService;

    public StaleSyncRunCleaner(SyncRunService syncRunService) {
        this.syncRunService = syncRunService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int stale = syncRunService.failStaleRuns();
        if (stale > 0) {
            log.warn("Marked {} stale RUNNING sync run(s) from a previous process as FAILED", stale);
        }
    }
}
