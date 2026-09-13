package com.quental.rickmorty.character.dto;

import com.quental.rickmorty.character.CharacterGender;
import com.quental.rickmorty.character.CharacterStatus;
import com.quental.rickmorty.episode.dto.EpisodeSummaryResponse;
import com.quental.rickmorty.location.dto.LocationSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CharacterDetail")
public final class CharacterDetailResponse {

    private final Long id;
    private final long externalId;
    private final String name;
    private final CharacterStatus status;
    @Schema(nullable = true)
    private final String species;
    @Schema(nullable = true)
    private final String type;
    private final CharacterGender gender;
    @Schema(nullable = true)
    private final String imageUrl;
    @Schema(description = "Origin location; null when unknown in the source", nullable = true)
    private final LocationSummaryResponse origin;
    @Schema(description = "Current location; null when unknown in the source", nullable = true)
    private final LocationSummaryResponse location;
    @Schema(description = "Episodes ordered by code")
    private final List<EpisodeSummaryResponse> episodes;

    public CharacterDetailResponse(Long id, long externalId, String name, CharacterStatus status, String species,
                                   String type, CharacterGender gender, String imageUrl,
                                   LocationSummaryResponse origin, LocationSummaryResponse location,
                                   List<EpisodeSummaryResponse> episodes) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.status = status;
        this.species = species;
        this.type = type;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.origin = origin;
        this.location = location;
        this.episodes = List.copyOf(episodes);
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

    public LocationSummaryResponse getOrigin() {
        return origin;
    }

    public LocationSummaryResponse getLocation() {
        return location;
    }

    public List<EpisodeSummaryResponse> getEpisodes() {
        return episodes;
    }
}
