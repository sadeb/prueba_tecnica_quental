package com.quental.rickmorty.character;

import com.quental.rickmorty.TestData;
import com.quental.rickmorty.episode.Episode;
import com.quental.rickmorty.episode.EpisodeJpaRepository;
import com.quental.rickmorty.episode.EpisodePersistenceService;
import com.quental.rickmorty.location.LocationJpaRepository;
import com.quental.rickmorty.location.LocationPersistenceService;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** H2 in PostgreSQL mode with the real Flyway migrations; no Docker (spec/07 point 3). */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({CharacterPersistenceService.class, LocationPersistenceService.class, EpisodePersistenceService.class})
class CharacterPersistenceServiceTest {

    @Autowired
    private CharacterPersistenceService service;
    @Autowired
    private CharacterJpaRepository characters;
    @Autowired
    private EpisodeJpaRepository episodes;
    @Autowired
    private LocationJpaRepository locations;
    @Autowired
    private EpisodePersistenceService episodeService;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldCreatePlaceholdersForUnknownReferences() {
        service.upsert(TestData.aCharacterSnapshot(1L, 1L, 20L, List.of(1L, 2L)));
        flushAndClear();

        Character saved = characters.findByExternalId(1L).orElseThrow();
        assertThat(saved.isPlaceholder()).isFalse();
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.ALIVE);
        assertThat(saved.getOrigin().getExternalId()).isEqualTo(1L);
        assertThat(saved.getOrigin().isPlaceholder()).isTrue();
        assertThat(saved.getOrigin().getName()).isEqualTo("Origin 1");
        assertThat(saved.getLocation().getExternalId()).isEqualTo(20L);
        assertThat(saved.getEpisodes()).extracting(Episode::getExternalId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(saved.getEpisodes()).allMatch(Episode::isPlaceholder);
        assertThat(episodes.count()).isEqualTo(2);
        assertThat(locations.count()).isEqualTo(2);
    }

    @Test
    void shouldNotDuplicateRowsWhenSameSnapshotIsUpsertedTwice() {
        CharacterSnapshot snapshot = TestData.aCharacterSnapshot(2L);
        service.upsert(snapshot);
        flushAndClear();
        long charactersBefore = characters.count();
        long episodesBefore = episodes.count();
        long linksBefore = characters.findByExternalId(2L).orElseThrow().getEpisodes().size();

        service.upsert(snapshot);
        flushAndClear();

        assertThat(characters.count()).isEqualTo(charactersBefore).isEqualTo(1);
        assertThat(episodes.count()).isEqualTo(episodesBefore);
        assertThat(characters.findByExternalId(2L).orElseThrow().getEpisodes()).hasSize((int) linksBefore);
    }

    @Test
    void shouldReplaceEpisodeSetAndUpdateAttributesOnNewSnapshot() {
        service.upsert(TestData.aCharacterSnapshot(3L, 1L, 20L, List.of(1L, 2L)));
        flushAndClear();

        CharacterSnapshot changed = new CharacterSnapshot(3L, "Renamed", "DEAD", "Alien", "Parasite", "FEMALE",
                null, null, null, 20L, "Location 20", List.of(2L, 3L));
        service.upsert(changed);
        flushAndClear();

        Character saved = characters.findByExternalId(3L).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Renamed");
        assertThat(saved.getStatus()).isEqualTo(CharacterStatus.DEAD);
        assertThat(saved.getGender()).isEqualTo(CharacterGender.FEMALE);
        assertThat(saved.getOrigin()).isNull();
        assertThat(saved.getEpisodes()).extracting(Episode::getExternalId).containsExactlyInAnyOrder(2L, 3L);
        assertThat(episodes.count()).isEqualTo(3);
    }

    @Test
    void shouldLeaveOriginNullWhenSnapshotHasNoReference() {
        service.upsert(TestData.aCharacterSnapshot(4L, null, null, List.of()));
        flushAndClear();

        Character saved = characters.findByExternalId(4L).orElseThrow();
        assertThat(saved.getOrigin()).isNull();
        assertThat(saved.getLocation()).isNull();
        assertThat(saved.getEpisodes()).isEmpty();
        assertThat(locations.count()).isZero();
    }

    @Test
    void shouldCompletePlaceholderWhenRealSnapshotArrivesLater() {
        service.upsert(TestData.aCharacterSnapshot(5L, 1L, 20L, List.of(7L)));
        flushAndClear();
        assertThat(episodes.findByExternalId(7L).orElseThrow().isPlaceholder()).isTrue();

        episodeService.upsert(TestData.anEpisodeSnapshot(7L));
        flushAndClear();

        Episode episode = episodes.findByExternalId(7L).orElseThrow();
        assertThat(episode.isPlaceholder()).isFalse();
        assertThat(episode.getName()).isEqualTo("Episode 7");
        assertThat(episode.getCode()).isEqualTo("S01E07");
        assertThat(episodes.count()).isEqualTo(1);
        List<Long> linked = characters.findByEpisodeId(episode.getId()).stream()
                .map(Character::getExternalId).collect(Collectors.toList());
        assertThat(linked).containsExactly(5L);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
