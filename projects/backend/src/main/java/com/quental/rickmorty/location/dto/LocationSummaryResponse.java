package com.quental.rickmorty.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LocationSummary")
public final class LocationSummaryResponse {

    @Schema(description = "Internal id (use in routes)", example = "1")
    private final Long id;
    @Schema(description = "Id in the external source", example = "1")
    private final long externalId;
    private final String name;
    @Schema(nullable = true)
    private final String type;
    @Schema(nullable = true)
    private final String dimension;

    public LocationSummaryResponse(Long id, long externalId, String name, String type, String dimension) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.type = type;
        this.dimension = dimension;
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
}
