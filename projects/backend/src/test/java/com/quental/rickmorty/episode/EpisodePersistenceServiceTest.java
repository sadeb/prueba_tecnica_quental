package com.quental.rickmorty.episode;

import com.quental.rickmorty.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(EpisodePersistenceService.class)
class EpisodePersistenceServiceTest {

    @Autowired
    private EpisodePersistenceService service;
    @Autowired
    private EpisodeJpaRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldUpsertIdempotentlyAndKeepAirDateAsText() {
        service.upsert(TestData.anEpisodeSnapshot(1L));
        service.upsert(TestData.anEpisodeSnapshot(1L));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.count()).isEqualTo(1);
        Episode saved = repository.findByExternalId(1L).orElseThrow();
        assertThat(saved.getAirDate()).isEqualTo("December 2, 2013");
        assertThat(saved.getCode()).isEqualTo("S01E01");
    }

    @Test
    void shouldCreatePlaceholdersOnlyForUnknownIds() {
        service.upsert(TestData.anEpisodeSnapshot(1L));
        entityManager.flush();

        List<Episode> resolved = service.findOrCreatePlaceholders(List.of(1L, 2L));
        entityManager.flush();
        entityManager.clear();

        assertThat(resolved).extracting(Episode::getExternalId).containsExactly(1L, 2L);
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findByExternalId(1L).orElseThrow().isPlaceholder()).isFalse();
        assertThat(repository.findByExternalId(2L).orElseThrow().isPlaceholder()).isTrue();
    }
}
