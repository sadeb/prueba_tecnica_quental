package com.quental.rickmorty.character.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RelatedCharacter", description = "Character sharing episodes with the requested one (resolved in Neo4j)")
public final class RelatedCharacterResponse {

    private final CharacterSummaryResponse character;
    @Schema(description = "Number of episodes in common", example = "51")
    private final long sharedEpisodes;

    public RelatedCharacterResponse(CharacterSummaryResponse character, long sharedEpisodes) {
        this.character = character;
        this.sharedEpisodes = sharedEpisodes;
    }

    public CharacterSummaryResponse getCharacter() {
        return character;
    }

    public long getSharedEpisodes() {
        return sharedEpisodes;
    }
}
