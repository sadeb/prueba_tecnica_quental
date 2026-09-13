package com.quental.rickmorty.character;

import com.quental.rickmorty.character.dto.CharacterDetailResponse;
import com.quental.rickmorty.character.dto.CharacterDtoMapper;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.character.dto.RelatedCharacterResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.graph.GraphRepository;
import com.quental.rickmorty.graph.RelatedCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Read side of characters: PostgreSQL for attributes, Neo4j for the relation query (ADR-004). */
@Service
public class CharacterQueryService {

    private static final Logger log = LoggerFactory.getLogger(CharacterQueryService.class);

    private final CharacterJpaRepository repository;
    private final GraphRepository graph;

    public CharacterQueryService(CharacterJpaRepository repository, GraphRepository graph) {
        this.repository = repository;
        this.graph = graph;
    }

    @Transactional(readOnly = true)
    public PageResponse<CharacterSummaryResponse> search(CharacterFilter filter, int page, int size) {
        Specification<Character> spec = Specification.where(CharacterSpecifications.notPlaceholder())
                .and(CharacterSpecifications.nameContains(filter.getName()))
                .and(CharacterSpecifications.statusIs(filter.getStatus()))
                .and(CharacterSpecifications.speciesIs(filter.getSpecies()))
                .and(CharacterSpecifications.genderIs(filter.getGender()));
        return PageResponse.from(repository.findAll(spec, PageRequest.of(page, size, Sort.by("id")))
                .map(CharacterDtoMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public CharacterDetailResponse getDetail(long id) {
        return CharacterDtoMapper.toDetail(findVisible(id));
    }

    /**
     * Graph gives the ordered (externalId, sharedEpisodes) list; attributes are read from PostgreSQL in one
     * query. Ids without a visible row (graph ahead of Postgres, ADR-004) are skipped with a warning.
     */
    @Transactional(readOnly = true)
    public List<RelatedCharacterResponse> getRelated(long id, int limit) {
        Character character = findVisible(id);
        List<RelatedCharacter> related = graph.findRelated(character.getExternalId(), limit);
        if (related.isEmpty()) {
            return List.of();
        }
        List<Long> externalIds = related.stream().map(RelatedCharacter::getExternalId).collect(Collectors.toList());
        Map<Long, Character> byExternalId = new HashMap<>();
        for (Character row : repository.findByExternalIdIn(externalIds)) {
            byExternalId.put(row.getExternalId(), row);
        }
        List<RelatedCharacterResponse> result = new ArrayList<>();
        for (RelatedCharacter entry : related) {
            Character row = byExternalId.get(entry.getExternalId());
            if (row == null || row.isPlaceholder()) {
                log.warn("Related character externalId={} present in graph but not (yet) in PostgreSQL; skipped",
                        entry.getExternalId());
                continue;
            }
            result.add(new RelatedCharacterResponse(CharacterDtoMapper.toSummary(row), entry.getSharedEpisodes()));
        }
        return result;
    }

    private Character findVisible(long id) {
        return repository.findByIdAndPlaceholderFalse(id).orElseThrow(() -> NotFoundException.of("Character", id));
    }
}
