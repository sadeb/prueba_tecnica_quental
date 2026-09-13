package com.quental.rickmorty.location;

import com.quental.rickmorty.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(LocationPersistenceService.class)
class LocationPersistenceServiceTest {

    @Autowired
    private LocationPersistenceService service;
    @Autowired
    private LocationJpaRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldUpsertIdempotentlyByExternalId() {
        service.upsert(TestData.aLocationSnapshot(1L));
        service.upsert(TestData.aLocationSnapshot(1L));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.count()).isEqualTo(1);
        Location saved = repository.findByExternalId(1L).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Location 1");
        assertThat(saved.isPlaceholder()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldTurnPlaceholderIntoRealRowKeepingTheSameId() {
        Location placeholder = repository.save(Location.placeholder(2L, "Earth"));
        entityManager.flush();

        Location upserted = service.upsert(TestData.aLocationSnapshot(2L));
        entityManager.flush();
        entityManager.clear();

        assertThat(upserted.getId()).isEqualTo(placeholder.getId());
        Location saved = repository.findById(placeholder.getId()).orElseThrow();
        assertThat(saved.isPlaceholder()).isFalse();
        assertThat(saved.getDimension()).isEqualTo("Dimension C-137");
    }

    @Test
    void shouldHidePlaceholderFromVisibleLookupUntilItsSnapshotArrives() {
        Location placeholder = repository.save(Location.placeholder(30L, "Somewhere"));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByIdAndPlaceholderFalse(placeholder.getId())).isEmpty();
        assertThat(repository.findByExternalId(30L)).map(Location::isPlaceholder).contains(true);
    }
}
