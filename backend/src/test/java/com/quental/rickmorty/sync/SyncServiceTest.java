package com.quental.rickmorty.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quental.rickmorty.sync.domain.SyncRunEntity;
import com.quental.rickmorty.sync.domain.SyncRunTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private SyncRunRepository repository;
    @Mock
    private SyncWorker worker;

    private SyncService service;

    @BeforeEach
    void setUp() {
        service = new SyncService(repository, worker);
        when(repository.saveAndFlush(any(SyncRunEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void registersManualRunsBeforeExecutingThem() {
        SyncRunEntity run = service.start();

        assertThat(run.getTrigger()).isEqualTo(SyncRunTrigger.MANUAL);
        verify(repository).saveAndFlush(run);
        verify(worker).execute(run.getId());
    }

    @Test
    void registersAutomaticRunsBeforeExecutingThem() {
        SyncRunEntity run = service.startAutomatically();

        assertThat(run.getTrigger()).isEqualTo(SyncRunTrigger.AUTOMATIC);
        verify(repository).saveAndFlush(run);
        verify(worker).execute(run.getId());
    }
}
