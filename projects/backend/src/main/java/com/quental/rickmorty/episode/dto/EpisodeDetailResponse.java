package com.quental.rickmorty.episode.dto;

import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "EpisodeDetail")
public final class EpisodeDetailResponse {

    private final Long id;
    private final long externalId;
    private final String name;
    private final String code;
    @Schema(nullable = true)
    private final String airDate;
    private final List<CharacterSummaryResponse> characters;

    public EpisodeDetailResponse(Long id, long externalId, String name, String code, String airDate,
                                 List<CharacterSummaryResponse> characters) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.code = code;
        this.airDate = airDate;
        this.characters = List.copyOf(characters);
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

    public String getCode() {
        return code;
    }

    public String getAirDate() {
        return airDate;
    }

    public List<CharacterSummaryResponse> getCharacters() {
        return characters;
    }
}
