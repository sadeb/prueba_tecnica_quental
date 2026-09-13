package com.quental.rickmorty.episode.dto;

import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.episode.Episode;

import java.util.List;

public final class EpisodeDtoMapper {

    private EpisodeDtoMapper() {
    }

    public static EpisodeSummaryResponse toSummary(Episode episode) {
        return new EpisodeSummaryResponse(episode.getId(), episode.getExternalId(), episode.getName(),
                episode.getCode(), episode.getAirDate());
    }

    public static EpisodeDetailResponse toDetail(Episode episode, List<CharacterSummaryResponse> characters) {
        return new EpisodeDetailResponse(episode.getId(), episode.getExternalId(), episode.getName(),
                episode.getCode(), episode.getAirDate(), characters);
    }
}
