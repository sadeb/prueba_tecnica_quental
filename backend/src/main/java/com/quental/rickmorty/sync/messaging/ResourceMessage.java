package com.quental.rickmorty.sync.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.quental.rickmorty.sync.external.ResourceType;
import java.time.Instant;

public class ResourceMessage {

    private String messageId;
    private String syncRunId;
    private ResourceType resourceType;
    private Long externalId;
    private int schemaVersion;
    private Instant occurredAt;
    private JsonNode payload;

    public ResourceMessage() {
    }

    public ResourceMessage(String messageId, String syncRunId, ResourceType resourceType, Long externalId, JsonNode payload) {
        this.messageId = messageId;
        this.syncRunId = syncRunId;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.schemaVersion = 1;
        this.occurredAt = Instant.now();
        this.payload = payload;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSyncRunId() { return syncRunId; }
    public void setSyncRunId(String syncRunId) { this.syncRunId = syncRunId; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public Long getExternalId() { return externalId; }
    public void setExternalId(Long externalId) { this.externalId = externalId; }
    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }
}
