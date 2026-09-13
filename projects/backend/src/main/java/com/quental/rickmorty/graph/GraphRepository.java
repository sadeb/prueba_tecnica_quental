package com.quental.rickmorty.graph;

import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.LocationSnapshot;

import java.util.List;

/**
 * Everything the application needs from the graph store. One interface so the consumer and the
 * query service depend on an abstraction and tests replace it with a mock (no Neo4j in `mvn test`).
 */
public interface GraphRepository {

    void ensureConstraints();

    void upsertLocation(LocationSnapshot snapshot);

    void upsertEpisode(EpisodeSnapshot snapshot);

    void upsertCharacter(CharacterSnapshot snapshot);

    /** Characters sharing episodes with the given one, most shared first. */
    List<RelatedCharacter> findRelated(long externalId, int limit);
}
