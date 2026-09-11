package com.quental.rickmorty.catalog.api;

import com.quental.rickmorty.catalog.domain.EpisodeEntity;

public class EpisodeResponse {
    private final Long id;
    private final Long externalId;
    private final String name;
    private final String airDate;
    private final String episodeCode;

    public EpisodeResponse(Long id, Long externalId, String name, String airDate, String episodeCode) {
        this.id = id; this.externalId = externalId; this.name = name; this.airDate = airDate; this.episodeCode = episodeCode;
    }
    public static EpisodeResponse from(EpisodeEntity value) {
        return new EpisodeResponse(value.getId(), value.getExternalId(), value.getName(), value.getAirDate(), value.getEpisodeCode());
    }
    public Long getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public String getAirDate() { return airDate; }
    public String getEpisodeCode() { return episodeCode; }
}
