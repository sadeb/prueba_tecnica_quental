package com.quental.rickmorty.catalog.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(name = "uk_locations_source_external", columnNames = {"source", "external_id"}))
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String source;
    @Column(name = "external_id", nullable = false)
    private Long externalId;
    private String name;
    @Column(name = "location_type")
    private String type;
    private String dimension;
    @Column(name = "source_url", length = 512)
    private String sourceUrl;
    @Column(name = "source_created_at")
    private Instant sourceCreatedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LocationEntity() {
    }

    public LocationEntity(String source, Long externalId) {
        this.source = source;
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void update(String name, String type, String dimension, String sourceUrl, Instant sourceCreatedAt) {
        this.name = name;
        this.type = type;
        this.dimension = dimension;
        this.sourceUrl = sourceUrl;
        this.sourceCreatedAt = sourceCreatedAt;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDimension() { return dimension; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getSourceCreatedAt() { return sourceCreatedAt; }
}
