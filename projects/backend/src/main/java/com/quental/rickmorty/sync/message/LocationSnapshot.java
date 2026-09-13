package com.quental.rickmorty.sync.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Full state of a location in the internal model; empty external fields are already null (ADR-001). */
public final class LocationSnapshot {

    private final long externalId;
    private final String name;
    private final String type;
    private final String dimension;

    @JsonCreator
    public LocationSnapshot(@JsonProperty("externalId") long externalId,
                            @JsonProperty("name") String name,
                            @JsonProperty("type") String type,
                            @JsonProperty("dimension") String dimension) {
        this.externalId = externalId;
        this.name = name;
        this.type = type;
        this.dimension = dimension;
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
