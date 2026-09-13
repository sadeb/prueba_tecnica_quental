package com.quental.rickmorty.sync.consumer;

import com.quental.rickmorty.character.CharacterPersistenceService;
import com.quental.rickmorty.episode.EpisodePersistenceService;
import com.quental.rickmorty.graph.GraphRepository;
import com.quental.rickmorty.location.LocationPersistenceService;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EntityType;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.InvalidMessageException;
import com.quental.rickmorty.sync.message.LocationSnapshot;
import com.quental.rickmorty.sync.message.SyncMessage;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Transformation + persistence side of the sync (spec/03 point 2). Per message: decode -> upsert in
 * PostgreSQL (own transaction, commits) -> MERGE in Neo4j. Any exception propagates to the container's
 * DefaultErrorHandler: retry with backoff, then DLT (ADR-004, ADR-006). Offsets are committed by the
 * container only after this method returns (AckMode.RECORD), never before persistence.
 *
 * ONE listener for the three topics = one consumer thread: characters are never upserted concurrently
 * with the episodes/locations they reference, so placeholder creation cannot race with the real upsert
 * on the same external_id (unique constraint). ADR-003 already renounces parallelism (1 partition).
 * No @Transactional here on purpose: the JPA transaction must be committed before the graph write.
 */
@Component
public class SyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(SyncConsumer.class);
    private static final long PROGRESS_EVERY = 100;

    private final SyncMessageCodec codec;
    private final LocationPersistenceService locations;
    private final EpisodePersistenceService episodes;
    private final CharacterPersistenceService characters;
    private final GraphRepository graph;
    private long processed;

    public SyncConsumer(SyncMessageCodec codec,
                        LocationPersistenceService locations,
                        EpisodePersistenceService episodes,
                        CharacterPersistenceService characters,
                        GraphRepository graph) {
        this.codec = codec;
        this.locations = locations;
        this.episodes = episodes;
        this.characters = characters;
        this.graph = graph;
    }

    @KafkaListener(topics = {"${sync.topics.locations}", "${sync.topics.episodes}", "${sync.topics.characters}"})
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        SyncMessage message = codec.decode(consumerRecord.value());
        switch (message.getEntityType()) {
            case LOCATION:
                LocationSnapshot location = codec.data(message, EntityType.LOCATION, LocationSnapshot.class);
                locations.upsert(location);
                graph.upsertLocation(location);
                break;
            case EPISODE:
                EpisodeSnapshot episode = codec.data(message, EntityType.EPISODE, EpisodeSnapshot.class);
                episodes.upsert(episode);
                graph.upsertEpisode(episode);
                break;
            case CHARACTER:
                CharacterSnapshot character = codec.data(message, EntityType.CHARACTER, CharacterSnapshot.class);
                characters.upsert(character);
                graph.upsertCharacter(character);
                break;
            default:
                throw new InvalidMessageException("Unsupported entityType " + message.getEntityType());
        }
        trace(consumerRecord, message);
    }

    private void trace(ConsumerRecord<String, String> consumerRecord, SyncMessage message) {
        processed++;
        log.debug("Persisted {} {} from {}@{}", message.getEntityType(), message.getExternalId(),
                consumerRecord.topic(), consumerRecord.offset());
        if (processed % PROGRESS_EVERY == 0) {
            log.info("Consumed {} sync messages so far (last: {} {})", processed, message.getEntityType(), message.getExternalId());
        }
    }
}
