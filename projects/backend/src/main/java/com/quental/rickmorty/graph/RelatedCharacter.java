package com.quental.rickmorty.graph;

/** Row of the shared-episodes query: who, and how many episodes in common. */
public final class RelatedCharacter {

    private final long externalId;
    private final long sharedEpisodes;

    public RelatedCharacter(long externalId, long sharedEpisodes) {
        this.externalId = externalId;
        this.sharedEpisodes = sharedEpisodes;
    }

    public long getExternalId() {
        return externalId;
    }

    public long getSharedEpisodes() {
        return sharedEpisodes;
    }
}
