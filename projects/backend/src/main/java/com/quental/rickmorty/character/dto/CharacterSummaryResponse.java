package com.quental.rickmorty.character.dto;

import com.quental.rickmorty.character.CharacterGender;
import com.quental.rickmorty.character.CharacterStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CharacterSummary")
public final class CharacterSummaryResponse {

    @Schema(description = "Internal id (use in routes)", example = "1")
    private final Long id;
    @Schema(description = "Id in the external source", example = "1")
    private final long externalId;
    @Schema(example = "Rick Sanchez")
    private final String name;
    private final CharacterStatus status;
    @Schema(example = "Human", nullable = true)
    private final String species;
    @Schema(description = "Sub-type; null when the source has none", nullable = true)
    private final String type;
    private final CharacterGender gender;
    @Schema(nullable = true)
    private final String imageUrl;

    public CharacterSummaryResponse(Long id, long externalId, String name, CharacterStatus status, String species,
                                    String type, CharacterGender gender, String imageUrl) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.status = status;
        this.species = species;
        this.type = type;
        this.gender = gender;
        this.imageUrl = imageUrl;
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
}
