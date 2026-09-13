package com.quental.rickmorty.character;

import com.quental.rickmorty.episode.Episode;
import com.quental.rickmorty.episode.EpisodePersistenceService;
import com.quental.rickmorty.location.Location;
import com.quental.rickmorty.location.LocationPersistenceService;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Upsert of a character snapshot in one transaction (ADR-002): attributes, origin/location
 * (placeholders when unknown, null when the source has no reference) and the episode set replaced.
 */
@Service
public class CharacterPersistenceService {

    private final CharacterJpaRepository repository;
    private final LocationPersistenceService locations;
    private final EpisodePersistenceService episodes;

    public CharacterPersistenceService(CharacterJpaRepository repository,
                                       LocationPersistenceService locations,
                                       EpisodePersistenceService episodes) {
        this.repository = repository;
        this.locations = locations;
        this.episodes = episodes;
    }

    @Transactional
    public Character upsert(CharacterSnapshot snapshot) {
        Character character = repository.findByExternalId(snapshot.getExternalId())
                .orElseGet(() -> Character.fresh(snapshot.getExternalId()));
        character.applyAttributes(snapshot.getName(),
                CharacterStatus.fromExternal(snapshot.getStatus()),
                snapshot.getSpecies(),
                snapshot.getType(),
                CharacterGender.fromExternal(snapshot.getGender()),
                snapshot.getImageUrl());

        Location origin = resolveLocation(snapshot.getOriginExternalId(), snapshot.getOriginName());
        Location location = resolveLocation(snapshot.getLocationExternalId(), snapshot.getLocationName());
        List<Episode> episodeRows = episodes.findOrCreatePlaceholders(new LinkedHashSet<>(snapshot.getEpisodeExternalIds()));
        character.applyRelations(origin, location, episodeRows);
        return repository.save(character);
    }

    private Location resolveLocation(Long externalId, String name) {
        if (externalId == null) {
            return null;
        }
        return locations.findOrCreatePlaceholder(externalId, name);
    }
}
