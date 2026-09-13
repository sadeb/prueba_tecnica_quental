package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncMessageCodecTest {

    private final ObjectMapper objectMapper = TestData.objectMapper();
    private final SyncMessageCodec codec = new SyncMessageCodec(objectMapper);

    @Test
    void shouldRoundTripCharacterMessage() {
        Instant fetchedAt = Instant.parse("2026-09-12T10:00:00Z");
        SyncMessage original = codec.build(7L, EntityType.CHARACTER, 1L, TestData.aCharacterSnapshot(1L), fetchedAt);

        String json = codec.encode(original);
        SyncMessage decoded = codec.decode(json);
        CharacterSnapshot snapshot = codec.data(decoded, EntityType.CHARACTER, CharacterSnapshot.class);

        assertThat(json).contains("\"schemaVersion\":1").contains("\"fetchedAt\":\"2026-09-12T10:00:00Z\"");
        assertThat(decoded.getSyncRunId()).isEqualTo(7L);
        assertThat(decoded.getEntityType()).isEqualTo(EntityType.CHARACTER);
        assertThat(snapshot.getExternalId()).isEqualTo(1L);
        assertThat(snapshot.getEpisodeExternalIds()).containsExactly(1L, 2L);
        assertThat(snapshot.getType()).isNull();
    }

    @Test
    void shouldRejectUnknownSchemaVersion() {
        String json = codec.encode(codec.build(null, EntityType.LOCATION, 1L, TestData.aLocationSnapshot(1L), Instant.now()))
                .replace("\"schemaVersion\":1", "\"schemaVersion\":99");

        assertThatThrownBy(() -> codec.decode(json))
                .isInstanceOf(InvalidMessageException.class)
                .hasMessageContaining("schemaVersion");
    }

    @Test
    void shouldRejectInvalidJson() {
        assertThatThrownBy(() -> codec.decode("{not-json")).isInstanceOf(InvalidMessageException.class);
    }

    @Test
    void shouldRejectMessageWithoutData() {
        assertThatThrownBy(() -> codec.decode("{\"schemaVersion\":1,\"entityType\":\"EPISODE\",\"externalId\":1}"))
                .isInstanceOf(InvalidMessageException.class)
                .hasMessageContaining("data");
    }

    @Test
    void shouldRejectDataOfAnotherEntityType() {
        SyncMessage message = codec.build(1L, EntityType.EPISODE, 1L, TestData.anEpisodeSnapshot(1L), Instant.now());

        assertThatThrownBy(() -> codec.data(message, EntityType.CHARACTER, CharacterSnapshot.class))
                .isInstanceOf(InvalidMessageException.class);
    }

    @Test
    void shouldExtractRunIdEvenFromAnOtherwiseInvalidMessage() {
        assertThat(codec.tryExtractRunId("{\"syncRunId\":42,\"schemaVersion\":99}")).contains(42L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"schemaVersion\":1}", "garbage", "{\"syncRunId\":\"abc\"}"})
    void shouldReturnEmptyRunIdWhenAbsentOrUnreadable(String json) {
        assertThat(codec.tryExtractRunId(json)).isEmpty();
    }

    @Test
    void shouldReturnEmptyRunIdForNullPayload() {
        assertThat(codec.tryExtractRunId(null)).isEmpty();
    }
}
