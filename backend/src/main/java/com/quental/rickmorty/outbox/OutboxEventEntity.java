package com.quental.rickmorty.outbox;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "aggregate_type", nullable = false, length = 40)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false, length = 80)
    private String aggregateId;
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;
    @Column(nullable = false, length = 160)
    private String topic;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(String aggregateType, String aggregateId, String eventType, String topic, String payload) {
        this.id = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.topic = topic;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void published() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void failed(String message) {
        this.status = OutboxStatus.FAILED;
        this.attempts++;
        this.lastError = message == null || message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    public String getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
}
