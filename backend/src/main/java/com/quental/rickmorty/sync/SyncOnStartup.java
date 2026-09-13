package com.quental.rickmorty.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SyncOnStartup {

    private final SyncService syncService;
    private final boolean enabled;

    public SyncOnStartup(SyncService syncService, @Value("${app.sync.on-startup:false}") boolean enabled) {
        this.syncService = syncService;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void synchronize() {
        if (enabled) syncService.startAutomatically();
    }
}
