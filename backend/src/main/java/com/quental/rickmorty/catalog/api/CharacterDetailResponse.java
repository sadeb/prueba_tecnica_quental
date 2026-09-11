package com.quental.rickmorty.catalog.api;

import com.quental.rickmorty.catalog.domain.CharacterEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterDetailResponse {
    private final CharacterSummaryResponse character;
    private final LocationResponse origin;
    private final LocationResponse currentLocation;
    private final List<EpisodeResponse> episodes;

    public CharacterDetailResponse(CharacterSummaryResponse character, LocationResponse origin,
                                   LocationResponse currentLocation, List<EpisodeResponse> episodes) {
        this.character = character; this.origin = origin; this.currentLocation = currentLocation; this.episodes = episodes;
    }
    public static CharacterDetailResponse from(CharacterEntity value) {
        List<EpisodeResponse> episodes = value.getEpisodes().stream()
                .sorted(Comparator.comparing(EpisodeEntityValue::code, Comparator.nullsLast(String::compareTo)))
                .map(EpisodeResponse::from).collect(Collectors.toList());
        return new CharacterDetailResponse(CharacterSummaryResponse.from(value), LocationResponse.from(value.getOrigin()),
                LocationResponse.from(value.getCurrentLocation()), episodes);
    }
    private static final class EpisodeEntityValue {
        private static String code(com.quental.rickmorty.catalog.domain.EpisodeEntity episode) { return episode.getEpisodeCode(); }
    }
    public CharacterSummaryResponse getCharacter() { return character; }
    public LocationResponse getOrigin() { return origin; }
    public LocationResponse getCurrentLocation() { return currentLocation; }
    public List<EpisodeResponse> getEpisodes() { return episodes; }
}
