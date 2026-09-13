package com.quental.rickmorty.episode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EpisodeJpaRepository extends JpaRepository<Episode, Long> {

    Optional<Episode> findByExternalId(long externalId);

    List<Episode> findByExternalIdIn(Collection<Long> externalIds);

    Optional<Episode> findByIdAndPlaceholderFalse(Long id);

    Page<Episode> findByPlaceholderFalse(Pageable pageable);
}
