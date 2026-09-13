package com.quental.rickmorty.graph;

public class RelatedCharacter {

    private final Long externalId;
    private final String name;
    private final long sharedEpisodes;

    public RelatedCharacter(Long externalId, String name, long sharedEpisodes) {
        this.externalId = externalId;
        this.name = name;
        this.sharedEpisodes = sharedEpisodes;
    }

    public Long getExternalId() { return externalId; }
    public String getName() { return name; }
    public long getSharedEpisodes() { return sharedEpisodes; }
}
