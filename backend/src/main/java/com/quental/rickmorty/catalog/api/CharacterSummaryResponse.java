package com.quental.rickmorty.catalog.api;

import com.quental.rickmorty.catalog.domain.CharacterEntity;

public class CharacterSummaryResponse {
    private final Long id;
    private final Long externalId;
    private final String name;
    private final String status;
    private final String species;
    private final String type;
    private final String gender;
    private final String imageUrl;

    public CharacterSummaryResponse(Long id, Long externalId, String name, String status, String species,
                                    String type, String gender, String imageUrl) {
        this.id = id; this.externalId = externalId; this.name = name; this.status = status;
        this.species = species; this.type = type; this.gender = gender; this.imageUrl = imageUrl;
    }
    public static CharacterSummaryResponse from(CharacterEntity value) {
        return new CharacterSummaryResponse(value.getId(), value.getExternalId(), value.getName(), value.getStatus(),
                value.getSpecies(), value.getType(), value.getGender(), value.getImageUrl());
    }
    public Long getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getSpecies() { return species; }
    public String getType() { return type; }
    public String getGender() { return gender; }
    public String getImageUrl() { return imageUrl; }
}
