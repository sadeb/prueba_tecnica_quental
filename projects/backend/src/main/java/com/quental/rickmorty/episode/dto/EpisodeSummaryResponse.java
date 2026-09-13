package com.quental.rickmorty.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EpisodeSummary")
public final class EpisodeSummaryResponse {

    @Schema(description = "Internal id (use in routes)", example = "1")
    private final Long id;
    @Schema(description = "Id in the external source", example = "1")
    private final long externalId;
    private final String name;
    @Schema(example = "S01E01")
    private final String code;
    @Schema(description = "Air date as published by the source (free text)", example = "December 2, 2013", nullable = true)
    private final String airDate;

    public EpisodeSummaryResponse(Long id, long externalId, String name, String code, String airDate) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.code = code;
        this.airDate = airDate;
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
}
