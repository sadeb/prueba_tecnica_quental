package com.quental.rickmorty.episode;

import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Idempotent upsert keyed by external_id (ADR-001, ADR-002) and placeholder creation (ADR-004). */
@Service
public class EpisodePersistenceService {

    private final EpisodeJpaRepository repository;

    public EpisodePersistenceService(EpisodeJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * The character list of an episode is not persisted here: the N:M set is owned by the character
     * snapshot (one writer per relation keeps the replace-on-upsert simple, ADR-002).
     */
    @Transactional
    public Episode upsert(EpisodeSnapshot snapshot) {
        Episode episode = repository.findByExternalId(snapshot.getExternalId())
                .orElseGet(() -> Episode.fresh(snapshot.getExternalId()));
        episode.applySnapshot(snapshot.getName(), snapshot.getAirDate(), snapshot.getCode());
        return repository.save(episode);
    }

    /** Existing rows plus new placeholders for unknown ids, in the caller's transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public List<Episode> findOrCreatePlaceholders(Collection<Long> externalIds) {
        Map<Long, Episode> byExternalId = new HashMap<>();
        for (Episode episode : repository.findByExternalIdIn(externalIds)) {
            byExternalId.put(episode.getExternalId(), episode);
        }
        List<Episode> result = new ArrayList<>();
        for (Long externalId : externalIds) {
            Episode episode = byExternalId.get(externalId);
            if (episode == null) {
                episode = repository.save(Episode.placeholder(externalId));
                byExternalId.put(externalId, episode);
            }
            result.add(episode);
        }
        return result;
    }
}
