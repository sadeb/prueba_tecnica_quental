package com.quental.rickmorty.sync.domain;

import com.quental.rickmorty.sync.external.ResourceType;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "raw_payloads")
public class RawPayloadEntity {

    @Id
    @Column(length = 36)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_run_id", nullable = false)
    private SyncRunEntity syncRun;
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 24)
    private ResourceType resourceType;
    @Column(name = "external_id", nullable = false)
    private Long externalId;
    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;
    @Column(nullable = false, columnDefinition = "text")
    private String payload;
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected RawPayloadEntity() {
    }

    public RawPayloadEntity(SyncRunEntity syncRun, ResourceType resourceType, Long externalId,
                            String contentHash, String payload) {
        this.id = UUID.randomUUID().toString();
        this.syncRun = syncRun;
        this.resourceType = resourceType;
        this.externalId = externalId;
        this.contentHash = contentHash;
        this.payload = payload;
        this.receivedAt = Instant.now();
    }

    public String getId() { return id; }
}
