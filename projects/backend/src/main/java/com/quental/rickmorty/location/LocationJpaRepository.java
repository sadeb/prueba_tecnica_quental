package com.quental.rickmorty.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationJpaRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByExternalId(long externalId);

    Optional<Location> findByIdAndPlaceholderFalse(Long id);

    Page<Location> findByPlaceholderFalse(Pageable pageable);
}
