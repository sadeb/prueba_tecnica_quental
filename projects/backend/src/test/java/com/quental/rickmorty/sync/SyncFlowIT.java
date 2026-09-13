package com.quental.rickmorty.sync;

import com.quental.rickmorty.TestAwait;
import com.quental.rickmorty.TestData;
import com.quental.rickmorty.character.Character;
import com.quental.rickmorty.character.CharacterJpaRepository;
import com.quental.rickmorty.episode.EpisodeJpaRepository;
import com.quental.rickmorty.graph.GraphRepository;
import com.quental.rickmorty.location.LocationJpaRepository;
import com.quental.rickmorty.sync.message.EntityType;
import com.quental.rickmorty.sync.message.SyncMessage;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Mandatory sync-cycle test (spec/07 point 3): publish -> consume -> persist, with an in-memory Kafka
 * broker and H2. Neo4j is replaced by a mock of GraphRepository; the mock is also the completion signal,
 * because the consumer calls it only after the PostgreSQL transaction has committed (ADR-004).
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        topics = {"rm.characters", "rm.episodes", "rm.locations", "rm.characters.DLT", "rm.episodes.DLT", "rm.locations.DLT"})
class SyncFlowIT {

    private static final String CHARACTERS = "rm.characters";
    private static final String CHARACTERS_DLT = "rm.characters.DLT";

    @MockBean
    private GraphRepository graphRepository;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private SyncMessageCodec codec;
    @Autowired
    private CharacterJpaRepository characters;
    @Autowired
    private EpisodeJpaRepository episodes;
    @Autowired
    private LocationJpaRepository locations;
    @Autowired
    private SyncRunJpaRepository syncRuns;
    @Autowired
    private EmbeddedKafkaBroker broker;

    @Test
    void shouldPersistCharacterAndPlaceholdersWhenMessageIsConsumed() {
        SyncRun run = syncRuns.save(SyncRun.started(Instant.now()));
        publish(CHARACTERS, encode(run.getId(), 1001L, List.of(9001L, 9002L), 8001L, 8002L));

        verify(graphRepository, timeout(20_000)).upsertCharacter(argThat(s -> s.getExternalId() == 1001L));

        Character saved = characters.findByExternalId(1001L).orElseThrow();
        assertThat(saved.isPlaceholder()).isFalse();
        assertThat(saved.getName()).isEqualTo("Character 1001");
        assertThat(episodes.findByExternalId(9001L).orElseThrow().isPlaceholder()).isTrue();
        assertThat(episodes.findByExternalId(9002L).orElseThrow().isPlaceholder()).isTrue();
        assertThat(locations.findByExternalId(8001L).orElseThrow().isPlaceholder()).isTrue();
        assertThat(locations.findByExternalId(8002L).orElseThrow().getName()).isEqualTo("Location 8002");
    }

    @Test
    void shouldNotDuplicateRowsWhenSameMessageIsConsumedTwice() {
        String payload = encode(null, 1002L, List.of(9101L), 8101L, 8101L);
        publish(CHARACTERS, payload);
        publish(CHARACTERS, payload);

        verify(graphRepository, timeout(20_000).times(2)).upsertCharacter(argThat(s -> s.getExternalId() == 1002L));

        assertThat(characters.findByExternalIdIn(List.of(1002L))).hasSize(1);
        assertThat(episodes.findByExternalIdIn(List.of(9101L))).hasSize(1);
        assertThat(locations.findByExternalId(8101L)).isPresent();
        assertThat(characters.findByEpisodeId(episodes.findByExternalId(9101L).orElseThrow().getId())).hasSize(1);
    }

    @Test
    void shouldSendCorruptJsonToDltAndKeepConsumingTheNextMessage() {
        try (Consumer<String, String> dltConsumer = dltConsumer()) {
            publish(CHARACTERS, "{not-json");
            publish(CHARACTERS, encode(null, 1003L, List.of(), null, null));

            verify(graphRepository, timeout(20_000)).upsertCharacter(argThat(s -> s.getExternalId() == 1003L));
            assertThat(characters.findByExternalId(1003L)).isPresent();

            ConsumerRecord<String, String> dead = KafkaTestUtils.getSingleRecord(dltConsumer, CHARACTERS_DLT, 20_000L);
            assertThat(dead.value()).isEqualTo("{not-json");
            assertThat(exceptionHeaders(dead)).anyMatch(h -> h.contains("InvalidMessageException"));
        }
    }

    @Test
    void shouldSendUnknownSchemaVersionToDltAndCountItOnTheRun() {
        SyncRun run = syncRuns.save(SyncRun.started(Instant.now()));
        String payload = encode(run.getId(), 1004L, List.of(), null, null).replace("\"schemaVersion\":1", "\"schemaVersion\":99");
        try (Consumer<String, String> dltConsumer = dltConsumer()) {
            publish(CHARACTERS, payload);

            ConsumerRecord<String, String> dead = KafkaTestUtils.getSingleRecord(dltConsumer, CHARACTERS_DLT, 20_000L);
            assertThat(dead.value()).contains("\"schemaVersion\":99");
        }
        // >= 1: a DLT record of another test without run id may also be attributed to the latest run.
        TestAwait.until(() -> syncRuns.findById(run.getId()).orElseThrow().getFailedMessages() >= 1L,
                Duration.ofSeconds(10), "failed_messages counter of run " + run.getId());
        assertThat(characters.findByExternalId(1004L)).isEmpty();
    }

    @Test
    void shouldPersistLocationAndEpisodeMessagesFromTheirOwnTopics() {
        publish("rm.locations", codec.encode(codec.build(null, EntityType.LOCATION, 8201L,
                TestData.aLocationSnapshot(8201L), Instant.now())));
        publish("rm.episodes", codec.encode(codec.build(null, EntityType.EPISODE, 9201L,
                TestData.anEpisodeSnapshot(9201L), Instant.now())));

        verify(graphRepository, timeout(20_000)).upsertLocation(argThat(s -> s.getExternalId() == 8201L));
        verify(graphRepository, timeout(20_000)).upsertEpisode(argThat(s -> s.getExternalId() == 9201L));

        assertThat(locations.findByExternalId(8201L).orElseThrow().getDimension()).isEqualTo("Dimension C-137");
        assertThat(episodes.findByExternalId(9201L).orElseThrow().getCode()).isEqualTo("S01E9201");
    }

    private void publish(String topic, String payload) {
        try {
            kafkaTemplate.send(topic, "key", payload).get();
        } catch (Exception ex) {
            throw new AssertionError("Could not publish test message", ex);
        }
    }

    private String encode(Long runId, long externalId, List<Long> episodeIds, Long originId, Long locationId) {
        SyncMessage message = codec.build(runId, EntityType.CHARACTER, externalId,
                TestData.aCharacterSnapshot(externalId, originId, locationId, episodeIds), Instant.now());
        return codec.encode(message);
    }

    private Consumer<String, String> dltConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("dlt-" + UUID.randomUUID(), "true", broker);
        // Only records published after this consumer is assigned: DLT records of other tests are ignored.
        // seekToEnd=true is required: consumeFromAnEmbeddedTopic seeks to the beginning and ignores auto.offset.reset.
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(), new StringDeserializer()).createConsumer();
        broker.consumeFromEmbeddedTopics(consumer, true, CHARACTERS_DLT);
        // seekToEnd is lazy: resolve the position now, before the test publishes, or the record is skipped.
        consumer.assignment().forEach(consumer::position);
        return consumer;
    }

    private static List<String> exceptionHeaders(ConsumerRecord<String, String> consumerRecord) {
        List<String> values = new ArrayList<>();
        for (Header header : consumerRecord.headers()) {
            if (header.key().startsWith("kafka_dlt-exception")) {
                values.add(new String(header.value(), StandardCharsets.UTF_8));
            }
        }
        return values;
    }
}
