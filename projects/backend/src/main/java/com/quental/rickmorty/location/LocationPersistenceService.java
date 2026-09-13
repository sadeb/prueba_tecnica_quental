package com.quental.rickmorty.location;

import com.quental.rickmorty.sync.message.LocationSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent upsert keyed by external_id (ADR-001, ADR-002) and placeholder creation (ADR-004). */
@Service
public class LocationPersistenceService {

    private final LocationJpaRepository repository;

    public LocationPersistenceService(LocationJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Location upsert(LocationSnapshot snapshot) {
        Location location = repository.findByExternalId(snapshot.getExternalId())
                .orElseGet(() -> Location.fresh(snapshot.getExternalId()));
        location.applySnapshot(snapshot.getName(), snapshot.getType(), snapshot.getDimension());
        return repository.save(location);
    }

    /** Returns the existing row or creates a placeholder; joins the caller's transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Location findOrCreatePlaceholder(long externalId, String name) {
        return repository.findByExternalId(externalId)
                .orElseGet(() -> repository.save(Location.placeholder(externalId, name)));
    }
}
