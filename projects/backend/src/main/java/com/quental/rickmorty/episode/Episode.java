package com.quental.rickmorty.episode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private long externalId;

    private String name;

    /** Free text in the source ("December 2, 2013"); never parsed (references/rick-and-morty-api.md). */
    @Column(name = "air_date")
    private String airDate;

    private String code;

    @Column(nullable = false)
    private boolean placeholder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Episode() {
    }

    public static Episode placeholder(long externalId) {
        Episode episode = new Episode();
        episode.externalId = externalId;
        episode.placeholder = true;
        return episode;
    }

    public static Episode fresh(long externalId) {
        Episode episode = new Episode();
        episode.externalId = externalId;
        return episode;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void applySnapshot(String name, String airDate, String code) {
        this.name = name;
        this.airDate = airDate;
        this.code = code;
        this.placeholder = false;
    }

    public Long getId() {
        return id;
    }

    public long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getAirDate() {
        return airDate;
    }

    public String getCode() {
        return code;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
