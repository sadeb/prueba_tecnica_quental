package com.quental.rickmorty.sync;

import static org.assertj.core.api.Assertions.assertThat;

import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.catalog.CharacterRepository;
import com.quental.rickmorty.graph.GraphProjectionService;
import com.quental.rickmorty.sync.domain.SyncRunEntity;
import com.quental.rickmorty.sync.external.ResourceType;
import com.quental.rickmorty.sync.messaging.ResourceMessage;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"rickmorty.raw.v1", "rickmorty.graph.v1", "rickmorty.raw.v1.DLT"})
@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.listener.auto-startup=true",
        "app.outbox.enabled=false"
})
class ResourceConsumptionIntegrationTest {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private SyncRunRepository syncRunRepository;
    @Autowired private CharacterRepository characterRepository;
    @Autowired private ProcessedMessageRepository processedMessageRepository;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @MockBean private GraphProjectionService graphProjectionService;

    @Test
    void consumesPersistsAndDeduplicatesACharacterMessage() throws Exception {
        SyncRunEntity run = SyncRunEntity.start();
        run.messageQueued();
        run.queued();
        syncRunRepository.saveAndFlush(run);

        ResourceMessage message = new ResourceMessage("message-1", run.getId(), ResourceType.CHARACTER, 1L,
                objectMapper.readTree("{"
                        + "\"id\":1,\"name\":\"Rick Sanchez\",\"status\":\"Alive\","
                        + "\"species\":\"Human\",\"type\":\"\",\"gender\":\"Male\","
                        + "\"origin\":{\"name\":\"Earth (C-137)\",\"url\":\"https://rickandmortyapi.com/api/location/1\"},"
                        + "\"location\":{\"name\":\"Citadel of Ricks\",\"url\":\"https://rickandmortyapi.com/api/location/3\"},"
                        + "\"image\":\"https://rickandmortyapi.com/api/character/avatar/1.jpeg\","
                        + "\"episode\":[\"https://rickandmortyapi.com/api/episode/1\"],"
                        + "\"url\":\"https://rickandmortyapi.com/api/character/1\","
                        + "\"created\":\"2017-11-04T18:48:46.250Z\"}"));
        String envelope = objectMapper.writeValueAsString(message);

        kafkaTemplate.send("rickmorty.raw.v1", "1", envelope).get();
        kafkaTemplate.send("rickmorty.raw.v1", "1", envelope).get();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(characterRepository.count()).isEqualTo(1);
            assertThat(processedMessageRepository.count()).isEqualTo(1);
            assertThat(syncRunRepository.findById(run.getId()).orElseThrow().getProcessedCount()).isEqualTo(1);
        });

        assertThat(characterRepository.findDetailedById(characterRepository.findAll().get(0).getId()))
                .get().satisfies(character -> {
            assertThat(character.getExternalId()).isEqualTo(1L);
            assertThat(character.getName()).isEqualTo("Rick Sanchez");
            assertThat(character.getEpisodes()).hasSize(1);
        });
    }
}
