package com.quental.rickmorty.sync;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncOnStartupTest {

    @Mock
    private SyncService syncService;

    @Test
    void registersAnAutomaticRunWhenStartupSyncIsEnabled() {
        new SyncOnStartup(syncService, true).synchronize();

        verify(syncService).startAutomatically();
    }

    @Test
    void doesNotStartARunWhenStartupSyncIsDisabled() {
        new SyncOnStartup(syncService, false).synchronize();

        verify(syncService, never()).startAutomatically();
    }
}
