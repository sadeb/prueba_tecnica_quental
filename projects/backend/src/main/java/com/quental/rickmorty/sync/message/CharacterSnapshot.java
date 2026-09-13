package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Full state of a character. originExternalId / locationExternalId are null when the source has
 * no reference ("unknown"); their names travel with the snapshot so placeholders get a name (ADR-004).
 */
public final class CharacterSnapshot {

    private final long externalId;
    private final String name;
    private final String status;
    private final String species;
    private final String type;
    private final String gender;
    private final String imageUrl;
    private final Long originExternalId;
    private final String originName;
    private final Long locationExternalId;
    private final String locationName;
    private final List<Long> episodeExternalIds;

    @JsonCreator
    public CharacterSnapshot(@JsonProperty("externalId") long externalId,
                             @JsonProperty("name") String name,
                             @JsonProperty("status") String status,
                             @JsonProperty("species") String species,
                             @JsonProperty("type") String type,
                             @JsonProperty("gender") String gender,
                             @JsonProperty("imageUrl") String imageUrl,
                             @JsonProperty("originExternalId") Long originExternalId,
                             @JsonProperty("originName") String originName,
                             @JsonProperty("locationExternalId") Long locationExternalId,
                             @JsonProperty("locationName") String locationName,
                             @JsonProperty("episodeExternalIds") List<Long> episodeExternalIds) {
        this.externalId = externalId;
        this.name = name;
        this.status = status;
        this.species = species;
        this.type = type;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.originExternalId = originExternalId;
        this.originName = originName;
        this.locationExternalId = locationExternalId;
        this.locationName = locationName;
        this.episodeExternalIds = episodeExternalIds == null ? List.of() : List.copyOf(episodeExternalIds);
    }

    public long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getSpecies() {
        return species;
    }

    public String getType() {
        return type;
    }

    public String getGender() {
        return gender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getOriginExternalId() {
        return originExternalId;
    }

    public String getOriginName() {
        return originName;
    }

    public Long getLocationExternalId() {
        return locationExternalId;
    }

    public String getLocationName() {
        return locationName;
    }

    public List<Long> getEpisodeExternalIds() {
        return episodeExternalIds;
    }
}
