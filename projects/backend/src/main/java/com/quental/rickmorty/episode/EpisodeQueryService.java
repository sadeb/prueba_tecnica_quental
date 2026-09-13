package com.quental.rickmorty.episode;

import com.quental.rickmorty.character.CharacterJpaRepository;
import com.quental.rickmorty.character.dto.CharacterDtoMapper;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.episode.dto.EpisodeDetailResponse;
import com.quental.rickmorty.episode.dto.EpisodeDtoMapper;
import com.quental.rickmorty.episode.dto.EpisodeSummaryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpisodeQueryService {

    private final EpisodeJpaRepository episodes;
    private final CharacterJpaRepository characters;

    public EpisodeQueryService(EpisodeJpaRepository episodes, CharacterJpaRepository characters) {
        this.episodes = episodes;
        this.characters = characters;
    }

    @Transactional(readOnly = true)
    public PageResponse<EpisodeSummaryResponse> list(int page, int size) {
        return PageResponse.from(episodes.findByPlaceholderFalse(PageRequest.of(page, size, Sort.by("id")))
                .map(EpisodeDtoMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public EpisodeDetailResponse getDetail(long id) {
        Episode episode = episodes.findByIdAndPlaceholderFalse(id)
                .orElseThrow(() -> NotFoundException.of("Episode", id));
        List<CharacterSummaryResponse> cast = characters.findByEpisodeId(id).stream()
                .map(CharacterDtoMapper::toSummary)
                .collect(Collectors.toList());
        return EpisodeDtoMapper.toDetail(episode, cast);
    }
}
