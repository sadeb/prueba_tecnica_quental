package com.quental.rickmorty.location.dto;

import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "LocationDetail")
public final class LocationDetailResponse {

    private final Long id;
    private final long externalId;
    private final String name;
    @Schema(nullable = true)
    private final String type;
    @Schema(nullable = true)
    private final String dimension;
    @Schema(description = "Characters whose current location is this one")
    private final List<CharacterSummaryResponse> residents;

    public LocationDetailResponse(Long id, long externalId, String name, String type, String dimension,
                                  List<CharacterSummaryResponse> residents) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.type = type;
        this.dimension = dimension;
        this.residents = List.copyOf(residents);
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

    public List<CharacterSummaryResponse> getResidents() {
        return residents;
    }
}
