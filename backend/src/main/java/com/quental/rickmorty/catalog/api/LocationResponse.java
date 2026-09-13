package com.quental.rickmorty.catalog.api;

import com.quental.rickmorty.catalog.domain.LocationEntity;

public class LocationResponse {
    private final Long id;
    private final Long externalId;
    private final String name;
    private final String type;
    private final String dimension;

    public LocationResponse(Long id, Long externalId, String name, String type, String dimension) {
        this.id = id; this.externalId = externalId; this.name = name; this.type = type; this.dimension = dimension;
    }
    public static LocationResponse from(LocationEntity value) {
        return value == null ? null : new LocationResponse(value.getId(), value.getExternalId(), value.getName(), value.getType(), value.getDimension());
    }
    public Long getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDimension() { return dimension; }
}
