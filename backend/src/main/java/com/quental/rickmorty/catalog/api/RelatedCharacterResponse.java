package com.quental.rickmorty.catalog.api;

public class RelatedCharacterResponse {
    private final Long id;
    private final Long externalId;
    private final String name;
    private final long sharedEpisodes;

    public RelatedCharacterResponse(Long id, Long externalId, String name, long sharedEpisodes) {
        this.id = id; this.externalId = externalId; this.name = name; this.sharedEpisodes = sharedEpisodes;
    }
    public Long getId() { return id; }
    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public long getSharedEpisodes() { return sharedEpisodes; }
}
