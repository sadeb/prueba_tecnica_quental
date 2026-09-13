package com.quental.rickmorty.location;

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
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private long externalId;

    private String name;
    private String type;
    private String dimension;

    @Column(nullable = false)
    private boolean placeholder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Location() {
    }

    public static Location placeholder(long externalId, String name) {
        Location location = new Location();
        location.externalId = externalId;
        location.name = name;
        location.placeholder = true;
        return location;
    }

    public static Location fresh(long externalId) {
        Location location = new Location();
        location.externalId = externalId;
        return location;
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

    public void applySnapshot(String name, String type, String dimension) {
        this.name = name;
        this.type = type;
        this.dimension = dimension;
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

    public String getType() {
        return type;
    }

    public String getDimension() {
        return dimension;
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
