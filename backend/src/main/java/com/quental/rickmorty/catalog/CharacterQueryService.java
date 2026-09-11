package com.quental.rickmorty.catalog;

import com.quental.rickmorty.catalog.api.CharacterDetailResponse;
import com.quental.rickmorty.catalog.api.CharacterSummaryResponse;
import com.quental.rickmorty.catalog.api.RelatedCharacterResponse;
import com.quental.rickmorty.catalog.domain.CharacterEntity;
import com.quental.rickmorty.graph.GraphProjectionService;
import com.quental.rickmorty.graph.RelatedCharacter;
import com.quental.rickmorty.shared.NotFoundException;
import com.quental.rickmorty.shared.PageResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.criteria.Expression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CharacterQueryService {

    private static final String SOURCE = "RICK_AND_MORTY";

    private final CharacterRepository repository;
    private final GraphProjectionService graphProjectionService;

    public CharacterQueryService(CharacterRepository repository, GraphProjectionService graphProjectionService) {
        this.repository = repository;
        this.graphProjectionService = graphProjectionService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CharacterSummaryResponse> search(String name, String status, String species,
                                                         String type, String gender, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending().and(Sort.by("id").ascending()));
        Specification<CharacterEntity> specification = Specification.where(containsIgnoreCase("name", name))
                .and(equalsIgnoreCase("status", status))
                .and(containsIgnoreCase("species", species))
                .and(containsIgnoreCase("type", type))
                .and(equalsIgnoreCase("gender", gender));
        Page<CharacterEntity> result = repository.findAll(specification, pageable);
        return PageResponse.from(result, CharacterSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public CharacterDetailResponse detail(Long id) {
        return CharacterDetailResponse.from(repository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Character not found")));
    }

    @Transactional(readOnly = true)
    public List<RelatedCharacterResponse> related(Long id, int limit) {
        CharacterEntity source = repository.findById(id).orElseThrow(() -> new NotFoundException("Character not found"));
        List<RelatedCharacter> related = graphProjectionService.findRelated(source.getExternalId(), limit);
        if (related.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, CharacterEntity> byExternalId = repository.findBySourceAndExternalIdIn(SOURCE,
                        related.stream().map(RelatedCharacter::getExternalId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(CharacterEntity::getExternalId, Function.identity()));
        return related.stream().filter(item -> byExternalId.containsKey(item.getExternalId()))
                .map(item -> new RelatedCharacterResponse(byExternalId.get(item.getExternalId()).getId(),
                        item.getExternalId(), item.getName(), item.getSharedEpisodes()))
                .collect(Collectors.toList());
    }

    private Specification<CharacterEntity> containsIgnoreCase(String field, String value) {
        if (!StringUtils.hasText(value)) return null;
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> builder.like(builder.lower(root.get(field)), pattern);
    }

    private Specification<CharacterEntity> equalsIgnoreCase(String field, String value) {
        if (!StringUtils.hasText(value)) return null;
        String expected = value.trim().toLowerCase(Locale.ROOT);
        return (root, query, builder) -> {
            Expression<String> expression = root.get(field);
            return builder.equal(builder.lower(expression), expected);
        };
    }
}
