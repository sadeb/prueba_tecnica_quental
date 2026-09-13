package com.quental.rickmorty.sync;

import com.quental.rickmorty.sync.producer.SyncProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Optional automatic sync at boot (SYNC_ON_STARTUP=true). Runs after the stale-run cleaner. */
@Component
@Order(10)
@ConditionalOnProperty(prefix = "sync", name = "on-startup", havingValue = "true")
public class SyncOnStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SyncOnStartupRunner.class);

    private final SyncProducerService producerService;

    public SyncOnStartupRunner(SyncProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    public void run(ApplicationArguments args) {
        SyncRun run = producerService.launch();
        log.info("sync.on-startup=true: launched sync run {}", run.getId());
    }
}
