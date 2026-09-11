package com.quental.rickmorty.sync.domain;

import com.quental.rickmorty.sync.external.ResourceType;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "processed_messages")
public class ProcessedMessageEntity {

    @Id
    @Column(name = "message_id", length = 80)
    private String messageId;
    @Column(name = "sync_run_id", nullable = false, length = 36)
    private String syncRunId;
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 24)
    private ResourceType resourceType;
    @Column(name = "external_id", nullable = false)
    private Long externalId;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedMessageEntity() {
    }

    public ProcessedMessageEntity(String messageId, String syncRunId, ResourceType resourceType, Long externalId) {
        this.messageId = messageId;
        this.syncRunId = syncRunId;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.processedAt = Instant.now();
    }
}
