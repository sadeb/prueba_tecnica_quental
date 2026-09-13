package com.quental.rickmorty.character.dto;

import com.quental.rickmorty.character.Character;
import com.quental.rickmorty.episode.Episode;
import com.quental.rickmorty.episode.dto.EpisodeDtoMapper;
import com.quental.rickmorty.episode.dto.EpisodeSummaryResponse;
import com.quental.rickmorty.location.dto.LocationDtoMapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Entity -> API DTO. Must run inside a transaction when lazy relations are touched (open-in-view=false). */
public final class CharacterDtoMapper {

    private CharacterDtoMapper() {
    }

    public static CharacterSummaryResponse toSummary(Character character) {
        return new CharacterSummaryResponse(character.getId(), character.getExternalId(), character.getName(),
                character.getStatus(), character.getSpecies(), character.getType(), character.getGender(),
                character.getImageUrl());
    }

    public static CharacterDetailResponse toDetail(Character character) {
        List<EpisodeSummaryResponse> episodes = character.getEpisodes().stream()
                .sorted(Comparator.comparing(Episode::getCode, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingLong(Episode::getExternalId))
                .map(EpisodeDtoMapper::toSummary)
                .collect(Collectors.toList());
        return new CharacterDetailResponse(character.getId(), character.getExternalId(), character.getName(),
                character.getStatus(), character.getSpecies(), character.getType(), character.getGender(),
                character.getImageUrl(), LocationDtoMapper.toSummary(character.getOrigin()),
                LocationDtoMapper.toSummary(character.getLocation()), episodes);
    }
}
