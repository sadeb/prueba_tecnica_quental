package com.quental.rickmorty.catalog.domain;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "characters", uniqueConstraints = @UniqueConstraint(name = "uk_characters_source_external", columnNames = {"source", "external_id"}))
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 32)
    private String source;
    @Column(name = "external_id", nullable = false)
    private Long externalId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 32)
    private String status;
    @Column(nullable = false)
    private String species;
    @Column(name = "character_type")
    private String type;
    @Column(nullable = false, length = 32)
    private String gender;
    @Column(name = "image_url", length = 512)
    private String imageUrl;
    @Column(name = "source_url", length = 512)
    private String sourceUrl;
    @Column(name = "source_created_at")
    private Instant sourceCreatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_location_id")
    private LocationEntity origin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_location_id")
    private LocationEntity currentLocation;
    @ManyToMany
    @JoinTable(name = "character_episodes",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "episode_id"))
    private Set<EpisodeEntity> episodes = new LinkedHashSet<>();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CharacterEntity() {
    }

    public CharacterEntity(String source, Long externalId) {
        this.source = source;
        this.externalId = externalId;
        this.updatedAt = Instant.now();
    }

    public void update(String name, String status, String species, String type, String gender,
                       String imageUrl, String sourceUrl, Instant sourceCreatedAt,
                       LocationEntity origin, LocationEntity currentLocation, Set<EpisodeEntity> episodes) {
        this.name = name;
        this.status = status;
        this.species = species;
        this.type = type;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.sourceUrl = sourceUrl;
        this.sourceCreatedAt = sourceCreatedAt;
        this.origin = origin;
        this.currentLocation = currentLocation;
        this.episodes.clear();
        this.episodes.addAll(episodes);
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getSpecies() { return species; }
    public String getType() { return type; }
    public String getGender() { return gender; }
    public String getImageUrl() { return imageUrl; }
    public String getSourceUrl() { return sourceUrl; }
    public Instant getSourceCreatedAt() { return sourceCreatedAt; }
    public LocationEntity getOrigin() { return origin; }
    public LocationEntity getCurrentLocation() { return currentLocation; }
    public Set<EpisodeEntity> getEpisodes() { return episodes; }
}
