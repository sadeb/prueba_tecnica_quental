package com.quental.rickmorty.character;

import com.quental.rickmorty.TestData;
import com.quental.rickmorty.character.dto.CharacterDetailResponse;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.episode.EpisodeJpaRepository;
import com.quental.rickmorty.episode.EpisodePersistenceService;
import com.quental.rickmorty.graph.GraphRepository;
import com.quental.rickmorty.location.LocationPersistenceService;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Filters and detail against real SQL on H2 (PostgreSQL mode), not against a mocked repository. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({CharacterQueryService.class, CharacterPersistenceService.class, LocationPersistenceService.class,
        EpisodePersistenceService.class})
class CharacterQueryServiceTest {

    @Autowired
    private CharacterQueryService service;
    @Autowired
    private CharacterPersistenceService persistence;
    @Autowired
    private EpisodePersistenceService episodes;
    @Autowired
    private EpisodeJpaRepository episodeRepository;
    @Autowired
    private TestEntityManager entityManager;
    @MockBean
    private GraphRepository graphRepository;

    @BeforeEach
    void seed() {
        persistence.upsert(snapshot(1L, "Rick Sanchez", "ALIVE", "Human", "MALE", List.of(1L, 2L)));
        persistence.upsert(snapshot(2L, "Morty Smith", "ALIVE", "Human", "MALE", List.of(1L)));
        persistence.upsert(snapshot(3L, "Birdperson", "DEAD", "Alien", "MALE", List.of(2L)));
        persistence.upsert(snapshot(4L, "Summer Smith", "ALIVE", "Human", "FEMALE", List.of()));
        episodes.upsert(TestData.anEpisodeSnapshot(1L));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldFilterByPartialNameCaseInsensitive() {
        PageResponse<CharacterSummaryResponse> page = service.search(new CharacterFilter("SMITH", null, null, null), 0, 20);

        assertThat(page.getContent()).extracting(CharacterSummaryResponse::getName)
                .containsExactly("Morty Smith", "Summer Smith");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldCombineStatusSpeciesAndGenderFilters() {
        PageResponse<CharacterSummaryResponse> page = service.search(
                new CharacterFilter(null, CharacterStatus.ALIVE, "human", CharacterGender.MALE), 0, 20);

        assertThat(page.getContent()).extracting(CharacterSummaryResponse::getExternalId).containsExactly(1L, 2L);
    }

    @Test
    void shouldReturnEmptyPagePastTheEndWithCorrectTotals() {
        PageResponse<CharacterSummaryResponse> page = service.search(new CharacterFilter(null, null, null, null), 5, 2);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getPage()).isEqualTo(5);
    }

    @Test
    void shouldBuildDetailWithOriginLocationAndEpisodesSortedByCode() {
        Long rickId = service.search(new CharacterFilter("rick", null, null, null), 0, 1).getContent().get(0).getId();

        CharacterDetailResponse detail = service.getDetail(rickId);

        assertThat(detail.getOrigin().getExternalId()).isEqualTo(1L);
        assertThat(detail.getLocation().getExternalId()).isEqualTo(20L);
        assertThat(detail.getEpisodes()).hasSize(2);
        assertThat(detail.getEpisodes().get(0).getCode()).isEqualTo("S01E01");
        assertThat(detail.getEpisodes().get(1).getCode()).isNull();
    }

    @Test
    void shouldHidePlaceholderEpisodeRowsFromDetailLookups() {
        Long placeholderEpisodeId = episodeRepository.findByExternalId(2L).orElseThrow().getId();

        assertThat(episodeRepository.findByIdAndPlaceholderFalse(placeholderEpisodeId)).isEmpty();
        assertThatThrownBy(() -> service.getDetail(999_999L)).isInstanceOf(NotFoundException.class);
    }

    private static CharacterSnapshot snapshot(long externalId, String name, String status, String species,
                                              String gender, List<Long> episodeIds) {
        return new CharacterSnapshot(externalId, name, status, species, null, gender, null,
                1L, "Earth", 20L, "Citadel", episodeIds);
    }
}
