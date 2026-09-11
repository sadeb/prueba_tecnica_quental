package com.quental.rickmorty.catalog;

import com.quental.rickmorty.catalog.domain.EpisodeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<EpisodeEntity, Long> {
    Optional<EpisodeEntity> findBySourceAndExternalId(String source, Long externalId);
}
