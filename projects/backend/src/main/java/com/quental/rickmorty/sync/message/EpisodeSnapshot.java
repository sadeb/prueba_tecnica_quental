package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class EpisodeSnapshot {

    private final long externalId;
    private final String name;
    private final String airDate;
    private final String code;
    private final List<Long> characterExternalIds;

    @JsonCreator
    public EpisodeSnapshot(@JsonProperty("externalId") long externalId,
                           @JsonProperty("name") String name,
                           @JsonProperty("airDate") String airDate,
                           @JsonProperty("code") String code,
                           @JsonProperty("characterExternalIds") List<Long> characterExternalIds) {
        this.externalId = externalId;
        this.name = name;
        this.airDate = airDate;
        this.code = code;
        this.characterExternalIds = characterExternalIds == null ? List.of() : List.copyOf(characterExternalIds);
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

    public List<Long> getCharacterExternalIds() {
        return characterExternalIds;
    }
}
