package com.quental.rickmorty.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.catalog.CharacterRepository;
import com.quental.rickmorty.catalog.EpisodeRepository;
import com.quental.rickmorty.catalog.LocationRepository;
import com.quental.rickmorty.catalog.domain.CharacterEntity;
import com.quental.rickmorty.catalog.domain.EpisodeEntity;
import com.quental.rickmorty.catalog.domain.LocationEntity;
import com.quental.rickmorty.config.TopicProperties;
import com.quental.rickmorty.outbox.OutboxEventEntity;
import com.quental.rickmorty.outbox.OutboxEventRepository;
import com.quental.rickmorty.sync.domain.ProcessedMessageEntity;
import com.quental.rickmorty.sync.domain.SyncRunEntity;
import com.quental.rickmorty.sync.external.ExternalApiException;
import com.quental.rickmorty.sync.external.ExternalResourceUrlParser;
import com.quental.rickmorty.sync.external.ResourceType;
import com.quental.rickmorty.sync.external.dto.RickMortyCharacterDto;
import com.quental.rickmorty.sync.external.dto.RickMortyEpisodeDto;
import com.quental.rickmorty.sync.external.dto.RickMortyLocationDto;
import com.quental.rickmorty.sync.messaging.ResourceMessage;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ResourceProcessor {

    private static final String SOURCE = "RICK_AND_MORTY";

    private final ObjectMapper objectMapper;
    private final CharacterRepository characterRepository;
    private final EpisodeRepository episodeRepository;
    private final LocationRepository locationRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final SyncRunRepository syncRunRepository;
    private final OutboxEventRepository outboxRepository;
    private final TopicProperties topics;

    public ResourceProcessor(ObjectMapper objectMapper, CharacterRepository characterRepository,
                             EpisodeRepository episodeRepository, LocationRepository locationRepository,
                             ProcessedMessageRepository processedMessageRepository, SyncRunRepository syncRunRepository,
                             OutboxEventRepository outboxRepository, TopicProperties topics) {
        this.objectMapper = objectMapper;
        this.characterRepository = characterRepository;
        this.episodeRepository = episodeRepository;
        this.locationRepository = locationRepository;
        this.processedMessageRepository = processedMessageRepository;
        this.syncRunRepository = syncRunRepository;
        this.outboxRepository = outboxRepository;
        this.topics = topics;
    }

    @Transactional
    public void process(ResourceMessage message) {
        validateEnvelope(message);
        if (processedMessageRepository.existsById(message.getMessageId())) {
            return;
        }
        switch (message.getResourceType()) {
            case LOCATION:
                upsertLocation(read(message, RickMortyLocationDto.class));
                break;
            case EPISODE:
                upsertEpisode(read(message, RickMortyEpisodeDto.class));
                break;
            case CHARACTER:
                upsertCharacter(read(message, RickMortyCharacterDto.class));
                break;
            default:
                throw new ExternalApiException("Unsupported resource type");
        }
        processedMessageRepository.save(new ProcessedMessageEntity(message.getMessageId(), message.getSyncRunId(),
                message.getResourceType(), message.getExternalId()));
        outboxRepository.save(new OutboxEventEntity(message.getResourceType().name(), String.valueOf(message.getExternalId()),
                "GRAPH_UPSERT", topics.getGraph(), write(message)));
        SyncRunEntity run = syncRunRepository.findById(message.getSyncRunId())
                .orElseThrow(() -> new IllegalStateException("Sync run not found"));
        run.processed();
        syncRunRepository.save(run);
    }

    @Transactional
    public void markFailed(ResourceMessage message) {
        syncRunRepository.findById(message.getSyncRunId()).ifPresent(run -> {
            run.failedMessage();
            syncRunRepository.save(run);
        });
    }

    private void upsertLocation(RickMortyLocationDto dto) {
        requireIdAndName(dto.getId(), dto.getName(), "location");
        LocationEntity entity = locationRepository.findBySourceAndExternalId(SOURCE, dto.getId())
                .orElseGet(() -> new LocationEntity(SOURCE, dto.getId()));
        entity.update(dto.getName(), emptyToNull(dto.getType()), emptyToNull(dto.getDimension()), dto.getUrl(), toInstant(dto.getCreated()));
        locationRepository.save(entity);
    }

    private void upsertEpisode(RickMortyEpisodeDto dto) {
        requireIdAndName(dto.getId(), dto.getName(), "episode");
        if (!StringUtils.hasText(dto.getEpisode())) {
            throw new ExternalApiException("Episode code is required");
        }
        EpisodeEntity entity = episodeRepository.findBySourceAndExternalId(SOURCE, dto.getId())
                .orElseGet(() -> new EpisodeEntity(SOURCE, dto.getId()));
        entity.update(dto.getName(), emptyToNull(dto.getAirDate()), dto.getEpisode(), dto.getUrl(), toInstant(dto.getCreated()));
        episodeRepository.save(entity);
    }

    private void upsertCharacter(RickMortyCharacterDto dto) {
        requireIdAndName(dto.getId(), dto.getName(), "character");
        if (!StringUtils.hasText(dto.getStatus()) || !StringUtils.hasText(dto.getSpecies()) || !StringUtils.hasText(dto.getGender())) {
            throw new ExternalApiException("Character status, species and gender are required");
        }
        LocationEntity origin = findOrCreateLocation(dto.getOrigin() == null ? null : dto.getOrigin().getUrl(),
                dto.getOrigin() == null ? null : dto.getOrigin().getName());
        LocationEntity current = findOrCreateLocation(dto.getLocation() == null ? null : dto.getLocation().getUrl(),
                dto.getLocation() == null ? null : dto.getLocation().getName());
        Set<EpisodeEntity> episodes = new LinkedHashSet<>();
        for (String url : dto.getEpisode()) {
            Long id = ExternalResourceUrlParser.extractId(url, ResourceType.EPISODE)
                    .orElseThrow(() -> new ExternalApiException("Character episode URL is required"));
            episodes.add(episodeRepository.findBySourceAndExternalId(SOURCE, id)
                    .orElseGet(() -> episodeRepository.save(new EpisodeEntity(SOURCE, id))));
        }
        CharacterEntity entity = characterRepository.findBySourceAndExternalId(SOURCE, dto.getId())
                .orElseGet(() -> new CharacterEntity(SOURCE, dto.getId()));
        entity.update(dto.getName(), dto.getStatus(), dto.getSpecies(), emptyToNull(dto.getType()), dto.getGender(),
                dto.getImage(), dto.getUrl(), toInstant(dto.getCreated()), origin, current, episodes);
        characterRepository.save(entity);
    }

    private LocationEntity findOrCreateLocation(String url, String fallbackName) {
        return ExternalResourceUrlParser.extractId(url, ResourceType.LOCATION)
                .map(id -> locationRepository.findBySourceAndExternalId(SOURCE, id)
                        .orElseGet(() -> {
                            LocationEntity location = new LocationEntity(SOURCE, id);
                            location.update(fallbackName, null, null, url, null);
                            return locationRepository.save(location);
                        }))
                .orElse(null);
    }

    private <T> T read(ResourceMessage message, Class<T> type) {
        try {
            return objectMapper.treeToValue(message.getPayload(), type);
        } catch (JsonProcessingException exception) {
            throw new ExternalApiException("Invalid " + message.getResourceType().name().toLowerCase() + " payload", exception);
        }
    }

    private String write(ResourceMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize graph event", exception);
        }
    }

    private void validateEnvelope(ResourceMessage message) {
        if (message == null || !StringUtils.hasText(message.getMessageId()) || !StringUtils.hasText(message.getSyncRunId())
                || message.getResourceType() == null || message.getExternalId() == null || message.getExternalId() <= 0
                || message.getSchemaVersion() != 1 || message.getPayload() == null || !message.getPayload().isObject()
                || !message.getPayload().path("id").canConvertToLong()
                || message.getPayload().path("id").asLong() != message.getExternalId()) {
            throw new ExternalApiException("Invalid resource message envelope");
        }
    }

    private void requireIdAndName(Long id, String name, String resource) {
        if (id == null || id <= 0 || !StringUtils.hasText(name)) {
            throw new ExternalApiException("Invalid " + resource + " identity");
        }
    }

    private Instant toInstant(java.time.OffsetDateTime value) { return value == null ? null : value.toInstant(); }
    private String emptyToNull(String value) { return StringUtils.hasText(value) ? value : null; }
}
