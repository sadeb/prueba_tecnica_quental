package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * JSON encoding/decoding of SyncMessage with the application ObjectMapper (java.time support).
 * Decoding validates the envelope: anything wrong is InvalidMessageException, which the consumer
 * error handler treats as non-retryable (ADR-006).
 */
@Component
public class SyncMessageCodec {

    private final ObjectMapper objectMapper;

    public SyncMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SyncMessage build(Long syncRunId, EntityType entityType, long externalId, Object snapshot, Instant fetchedAt) {
        return new SyncMessage(SyncMessage.CURRENT_SCHEMA_VERSION, syncRunId, entityType, externalId, fetchedAt,
                objectMapper.valueToTree(snapshot));
    }

    public String encode(SyncMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialise sync message", ex);
        }
    }

    public SyncMessage decode(String json) {
        SyncMessage message;
        try {
            message = objectMapper.readValue(json, SyncMessage.class);
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new InvalidMessageException("Malformed sync message: " + ex.getMessage(), ex);
        }
        if (message.getSchemaVersion() != SyncMessage.CURRENT_SCHEMA_VERSION) {
            throw new InvalidMessageException("Unsupported schemaVersion " + message.getSchemaVersion());
        }
        if (message.getEntityType() == null) {
            throw new InvalidMessageException("Missing entityType");
        }
        if (message.getExternalId() <= 0) {
            throw new InvalidMessageException("Invalid externalId " + message.getExternalId());
        }
        if (message.getData() == null || message.getData().isNull() || !message.getData().isObject()) {
            throw new InvalidMessageException("Missing data object");
        }
        return message;
    }

    public <T> T data(SyncMessage message, EntityType expectedType, Class<T> type) {
        if (message.getEntityType() != expectedType) {
            throw new InvalidMessageException("Expected entityType " + expectedType + " but was " + message.getEntityType());
        }
        try {
            return objectMapper.treeToValue(message.getData(), type);
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new InvalidMessageException("Invalid " + expectedType + " snapshot: " + ex.getMessage(), ex);
        }
    }

    /** Lenient: used only to attribute a dead-lettered message to a run, so it never throws. */
    public Optional<Long> tryExtractRunId(String json) {
        if (json == null) {
            return Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(json).get("syncRunId");
            return node != null && node.canConvertToLong() ? Optional.of(node.asLong()) : Optional.empty();
        } catch (JsonProcessingException | RuntimeException ex) {
            return Optional.empty();
        }
    }
}
