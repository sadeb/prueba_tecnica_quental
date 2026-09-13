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
@Table(name = "episodes", uniqueConstraints = @UniqueConstraint(name = "uk_episodes_source_external", columnNames = {"source", "external_id"}))
public class EpisodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String source;
    @Column(name = "external_id", nullable = false)
    private Long externalId;
    private String name;
    @Column(name = "air_date", length = 64)
    private String airDate;
    @Column(name = "episode_code", length = 16)
    private String episodeCode;
    @Column(name = "source_url", length = 512)
    private String sourceUrl;
    @Column(name = "source_created_at")
    private Instant sourceCreatedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EpisodeEntity() {
    }

    public EpisodeEntity(String source, Long externalId) {
        this.source = source;
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void update(String name, String airDate, String episodeCode, String sourceUrl, Instant sourceCreatedAt) {
        this.name = name;
        this.airDate = airDate;
        this.episodeCode = episodeCode;
        this.sourceUrl = sourceUrl;
        this.sourceCreatedAt = sourceCreatedAt;
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getAirDate() { return airDate; }
    public String getEpisodeCode() { return episodeCode; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getSourceCreatedAt() { return sourceCreatedAt; }
}
