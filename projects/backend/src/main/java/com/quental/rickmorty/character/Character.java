package com.quental.rickmorty.character;

import com.quental.rickmorty.episode.Episode;
import com.quental.rickmorty.location.Location;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Note: this class shadows java.lang.Character inside its package; qualify the JDK one if ever needed.
 * `placeholder` is kept for symmetry with Location/Episode and as a defensive filter: no flow creates
 * character placeholders today (the episode snapshot does not write the N:M side, see EpisodePersistenceService).
 */
@Entity
@Table(name = "characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private long externalId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CharacterStatus status = CharacterStatus.UNKNOWN;

    private String species;
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CharacterGender gender = CharacterGender.UNKNOWN;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id")
    private Location origin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "character_episodes",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "episode_id"))
    private Set<Episode> episodes = new HashSet<>();

    @Column(nullable = false)
    private boolean placeholder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Character() {
    }

    public static Character fresh(long externalId) {
        Character character = new Character();
        character.externalId = externalId;
        return character;
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

    public void applyAttributes(String name, CharacterStatus status, String species, String type,
                                CharacterGender gender, String imageUrl) {
        this.name = name;
        this.status = status == null ? CharacterStatus.UNKNOWN : status;
        this.species = species;
        this.type = type;
        this.gender = gender == null ? CharacterGender.UNKNOWN : gender;
        this.imageUrl = imageUrl;
        this.placeholder = false;
    }

    public void applyRelations(Location origin, Location location, Collection<Episode> episodes) {
        this.origin = origin;
        this.location = location;
        // Replace the whole N:M set with the snapshot's (ADR-002): removed episodes disappear.
        this.episodes.clear();
        this.episodes.addAll(episodes);
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

    public CharacterStatus getStatus() {
        return status;
    }

    public String getSpecies() {
        return species;
    }

    public String getType() {
        return type;
    }

    public CharacterGender getGender() {
        return gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getLocation() {
        return location;
    }

    public Set<Episode> getEpisodes() {
        return episodes;
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
