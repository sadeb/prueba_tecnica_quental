package com.quental.rickmorty.sync;

import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncRunServiceTest {

    private final SyncRunJpaRepository repository = mock(SyncRunJpaRepository.class);
    private final SyncRunService service = new SyncRunService(repository,
            Clock.fixed(Instant.parse("2026-09-13T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void shouldRejectStartWhenARunIsAlreadyRunning() {
        when(repository.existsByStatus(SyncRunStatus.RUNNING)).thenReturn(true);

        assertThatThrownBy(service::start).isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreateRunningRowWhenNothingIsRunning() {
        when(repository.existsByStatus(SyncRunStatus.RUNNING)).thenReturn(false);
        when(repository.save(any(SyncRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SyncRun run = service.start();

        assertThat(run.getStatus()).isEqualTo(SyncRunStatus.RUNNING);
        assertThat(run.getStartedAt()).isEqualTo(Instant.parse("2026-09-13T10:00:00Z"));
    }

    @Test
    void shouldAttributeFailedMessageToLatestRunWhenPayloadHasNoRunId() {
        SyncRun latest = SyncRun.started(Instant.now());
        ReflectionTestUtils.setField(latest, "id", 3L);
        when(repository.findFirstByOrderByStartedAtDesc()).thenReturn(Optional.of(latest));

        service.recordFailedMessage(Optional.empty());

        verify(repository).incrementFailedMessages(3L);
    }

    @Test
    void shouldOnlyWarnWhenThereIsNoRunToAttributeTheFailureTo() {
        when(repository.findFirstByOrderByStartedAtDesc()).thenReturn(Optional.empty());

        service.recordFailedMessage(Optional.empty());

        verify(repository, never()).incrementFailedMessages(any());
    }

    @Test
    void shouldUseGivenRunIdWhenItExists() {
        when(repository.existsById(7L)).thenReturn(true);

        service.recordFailedMessage(Optional.of(7L));

        verify(repository).incrementFailedMessages(7L);
        verify(repository, never()).findFirstByOrderByStartedAtDesc();
    }

    @Test
    void shouldFailWith404WhenRunDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }
}
