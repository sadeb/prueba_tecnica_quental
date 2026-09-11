package com.quental.rickmorty.catalog;

import com.quental.rickmorty.catalog.domain.LocationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    Optional<LocationEntity> findBySourceAndExternalId(String source, Long externalId);
}
