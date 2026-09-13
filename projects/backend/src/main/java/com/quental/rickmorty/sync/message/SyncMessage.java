package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Kafka payload envelope (ADR-003). `data` is the internal snapshot, never the provider JSON, so the
 * consumer does not know the external format. schemaVersion travels in the body so the DLT keeps it.
 */
public final class SyncMessage {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final Long syncRunId;
    private final EntityType entityType;
    private final long externalId;
    private final Instant fetchedAt;
    private final JsonNode data;

    @JsonCreator
    public SyncMessage(@JsonProperty("schemaVersion") int schemaVersion,
                       @JsonProperty("syncRunId") Long syncRunId,
                       @JsonProperty("entityType") EntityType entityType,
                       @JsonProperty("externalId") long externalId,
                       @JsonProperty("fetchedAt") Instant fetchedAt,
                       @JsonProperty("data") JsonNode data) {
        this.schemaVersion = schemaVersion;
        this.syncRunId = syncRunId;
        this.entityType = entityType;
        this.externalId = externalId;
        this.fetchedAt = fetchedAt;
        this.data = data;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public Long getSyncRunId() {
        return syncRunId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public long getExternalId() {
        return externalId;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public JsonNode getData() {
        return data;
    }
}
