package com.quental.rickmorty.sync.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.TestData;
import com.quental.rickmorty.common.ExternalServiceException;
import com.quental.rickmorty.external.ExternalCharacter;
import com.quental.rickmorty.external.ExternalEpisode;
import com.quental.rickmorty.external.ExternalLocation;
import com.quental.rickmorty.external.ExternalPage;
import com.quental.rickmorty.external.ExternalPageInfo;
import com.quental.rickmorty.external.ExternalPayloadValidator;
import com.quental.rickmorty.external.ExternalSnapshotMapper;
import com.quental.rickmorty.external.RickAndMortyClient;
import com.quental.rickmorty.sync.SyncProperties;
import com.quental.rickmorty.sync.SyncRunService;
import com.quental.rickmorty.sync.SyncRunStatus;
import com.quental.rickmorty.sync.message.EntityType;
import com.quental.rickmorty.sync.message.SyncMessage;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyncProducerServiceTest {

    private static final long RUN_ID = 5L;

    private final RickAndMortyClient client = mock(RickAndMortyClient.class);
    private final SyncPublisher publisher = mock(SyncPublisher.class);
    private final SyncRunService runService = mock(SyncRunService.class);
    private final ObjectMapper objectMapper = TestData.objectMapper();
    private SyncProducerService service;

    @BeforeEach
    void setUp() {
        service = new SyncProducerService(client, new ExternalPayloadValidator(), new ExternalSnapshotMapper(),
                publisher, new SyncMessageCodec(objectMapper), runService, new SyncProperties(), new SyncTaskExecutor());
        when(client.fetchLocations(anyInt())).thenReturn(page(List.of(location(1L)), null));
        when(client.fetchEpisodes(anyInt())).thenReturn(page(List.of(episode(1L)), null));
    }

    @Test
    void shouldWalkAllPagesInOrderLocationsEpisodesCharactersAndComplete() {
        when(client.fetchCharacters(1)).thenReturn(page(List.of(character(1L), character(2L)), "next"));
        when(client.fetchCharacters(2)).thenReturn(page(List.of(character(3L)), null));

        service.run(RUN_ID);

        ArgumentCaptor<String> topics = ArgumentCaptor.forClass(String.class);
        verify(publisher, times(5)).publish(topics.capture(), any(SyncMessage.class));
        assertThat(topics.getAllValues()).containsExactly("rm.locations", "rm.episodes", "rm.characters", "rm.characters", "rm.characters");
        verify(client, never()).fetchCharacters(3);
        verify(runService).finish(RUN_ID, SyncRunStatus.COMPLETED, 5L, 0L, 0L);
        InOrder inOrder = inOrder(client);
        inOrder.verify(client).fetchLocations(1);
        inOrder.verify(client).fetchEpisodes(1);
        inOrder.verify(client).fetchCharacters(1);
    }

    @Test
    void shouldMarkRunPartialAndContinueWithNextEntityWhenAPageFails() {
        when(client.fetchEpisodes(1)).thenThrow(new ExternalServiceException("boom"));
        when(client.fetchCharacters(1)).thenReturn(page(List.of(character(1L)), null));

        service.run(RUN_ID);

        verify(publisher, times(2)).publish(any(), any(SyncMessage.class));
        verify(runService).finish(RUN_ID, SyncRunStatus.PARTIAL, 2L, 0L, 1L);
    }

    @Test
    void shouldSkipInvalidElementAndKeepPublishingTheRest() {
        ExternalCharacter invalid = character(9L);
        invalid.setEpisode(List.of("https://x/api/episode/not-a-number"));
        when(client.fetchCharacters(1)).thenReturn(page(List.of(invalid, character(1L)), null));

        service.run(RUN_ID);

        ArgumentCaptor<SyncMessage> messages = ArgumentCaptor.forClass(SyncMessage.class);
        verify(publisher, times(3)).publish(any(), messages.capture());
        List<Long> characterIds = new ArrayList<>();
        for (SyncMessage message : messages.getAllValues()) {
            if (message.getEntityType() == EntityType.CHARACTER) {
                characterIds.add(message.getExternalId());
            }
        }
        assertThat(characterIds).containsExactly(1L);
        verify(runService).finish(RUN_ID, SyncRunStatus.COMPLETED, 3L, 1L, 0L);
    }

    @Test
    void shouldCountPageAsFailedWhenPublishingFails() {
        when(client.fetchCharacters(1)).thenReturn(page(List.of(character(1L), character(2L)), "next"));
        doThrow(new SyncPublishException("kafka down", new RuntimeException()))
                .when(publisher).publish(eq("rm.characters"), any(SyncMessage.class));

        service.run(RUN_ID);

        verify(client, never()).fetchCharacters(2);
        verify(runService).finish(RUN_ID, SyncRunStatus.PARTIAL, 2L, 0L, 1L);
    }

    @Test
    void shouldMarkRunFailedWhenSomethingUnexpectedHappens() {
        when(client.fetchCharacters(1)).thenThrow(new IllegalStateException("bug"));

        service.run(RUN_ID);

        verify(runService).finish(eq(RUN_ID), eq(SyncRunStatus.FAILED), eq(2L), eq(0L), eq(0L));
    }

    private static <T> ExternalPage<T> page(List<T> results, String next) {
        ExternalPage<T> page = new ExternalPage<>();
        ExternalPageInfo info = new ExternalPageInfo();
        info.setPages(2);
        info.setNext(next);
        page.setInfo(info);
        page.setResults(results);
        return page;
    }

    private static ExternalLocation location(long id) {
        ExternalLocation location = new ExternalLocation();
        location.setId(id);
        location.setName("Location " + id);
        return location;
    }

    private static ExternalEpisode episode(long id) {
        ExternalEpisode episode = new ExternalEpisode();
        episode.setId(id);
        episode.setName("Episode " + id);
        episode.setEpisode("S01E01");
        return episode;
    }

    private static ExternalCharacter character(long id) {
        ExternalCharacter character = new ExternalCharacter();
        character.setId(id);
        character.setName("Character " + id);
        character.setStatus("Alive");
        character.setGender("Male");
        character.setEpisode(List.of("https://x/api/episode/1"));
        return character;
    }
}
