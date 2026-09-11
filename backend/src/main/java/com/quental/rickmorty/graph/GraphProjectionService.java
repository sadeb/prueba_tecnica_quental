package com.quental.rickmorty.graph;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.sync.external.ExternalResourceUrlParser;
import com.quental.rickmorty.sync.external.ResourceType;
import com.quental.rickmorty.sync.external.dto.RickMortyCharacterDto;
import com.quental.rickmorty.sync.external.dto.RickMortyEpisodeDto;
import com.quental.rickmorty.sync.external.dto.RickMortyLocationDto;
import com.quental.rickmorty.sync.messaging.ResourceMessage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
public class GraphProjectionService {

    private static final String SOURCE = "RICK_AND_MORTY";

    private final Neo4jClient neo4jClient;
    private final ObjectMapper objectMapper;

    public GraphProjectionService(Neo4jClient neo4jClient, ObjectMapper objectMapper) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = objectMapper;
    }

    public void project(ResourceMessage message) {
        switch (message.getResourceType()) {
            case LOCATION:
                projectLocation(read(message, RickMortyLocationDto.class));
                break;
            case EPISODE:
                projectEpisode(read(message, RickMortyEpisodeDto.class));
                break;
            case CHARACTER:
                projectCharacter(read(message, RickMortyCharacterDto.class));
                break;
            default:
                throw new IllegalArgumentException("Unsupported graph resource");
        }
    }

    public List<RelatedCharacter> findRelated(Long externalId, int limit) {
        return neo4jClient.query("MATCH (source:Character {source: $source, externalId: $externalId})-[:APPEARED_IN]->(episode:Episode)<-[:APPEARED_IN]-(related:Character) "
                        + "WHERE related.externalId <> source.externalId "
                        + "RETURN related.externalId AS externalId, related.name AS name, count(episode) AS sharedEpisodes "
                        + "ORDER BY sharedEpisodes DESC, name ASC LIMIT $limit")
                .bind(SOURCE).to("source")
                .bind(externalId).to("externalId")
                .bind(limit).to("limit")
                .fetchAs(RelatedCharacter.class)
                .mappedBy((typeSystem, record) -> new RelatedCharacter(
                        record.get("externalId").asLong(),
                        record.get("name").isNull() ? "Unknown" : record.get("name").asString(),
                        record.get("sharedEpisodes").asLong()))
                .all().stream().collect(Collectors.toList());
    }

    private void projectLocation(RickMortyLocationDto dto) {
        neo4jClient.query("MERGE (location:Location {source: $source, externalId: $externalId}) "
                        + "SET location.name = $name, location.type = $type, location.dimension = $dimension")
                .bind(SOURCE).to("source")
                .bind(dto.getId()).to("externalId")
                .bindAll(java.util.Map.of(
                        "name", safe(dto.getName()),
                        "type", safe(dto.getType()),
                        "dimension", safe(dto.getDimension())))
                .run();
    }

    private void projectEpisode(RickMortyEpisodeDto dto) {
        neo4jClient.query("MERGE (episode:Episode {source: $source, externalId: $externalId}) "
                        + "SET episode.name = $name, episode.code = $code, episode.airDate = $airDate")
                .bind(SOURCE).to("source")
                .bind(dto.getId()).to("externalId")
                .bindAll(java.util.Map.of(
                        "name", safe(dto.getName()),
                        "code", safe(dto.getEpisode()),
                        "airDate", safe(dto.getAirDate())))
                .run();
    }

    private void projectCharacter(RickMortyCharacterDto dto) {
        Optional<Long> originId = ExternalResourceUrlParser.extractId(
                dto.getOrigin() == null ? null : dto.getOrigin().getUrl(), ResourceType.LOCATION);
        Optional<Long> currentId = ExternalResourceUrlParser.extractId(
                dto.getLocation() == null ? null : dto.getLocation().getUrl(), ResourceType.LOCATION);
        List<Long> episodeIds = dto.getEpisode().stream()
                .map(url -> ExternalResourceUrlParser.extractId(url, ResourceType.EPISODE).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        neo4jClient.query("MERGE (character:Character {source: $source, externalId: $externalId}) "
                        + "SET character.name = $name, character.status = $status, character.species = $species, character.imageUrl = $imageUrl "
                        + "WITH character OPTIONAL MATCH (character)-[old:APPEARED_IN|ORIGINATED_FROM|CURRENTLY_LOCATED_AT]->() DELETE old")
                .bind(SOURCE).to("source")
                .bind(dto.getId()).to("externalId")
                .bindAll(java.util.Map.of(
                        "name", safe(dto.getName()),
                        "status", safe(dto.getStatus()),
                        "species", safe(dto.getSpecies()),
                        "imageUrl", safe(dto.getImage())))
                .run();

        if (!episodeIds.isEmpty()) {
            neo4jClient.query("MATCH (character:Character {source: $source, externalId: $externalId}) "
                            + "UNWIND $episodeIds AS episodeId "
                            + "MERGE (episode:Episode {source: $source, externalId: episodeId}) "
                            + "MERGE (character)-[:APPEARED_IN]->(episode)")
                    .bind(SOURCE).to("source")
                    .bind(dto.getId()).to("externalId")
                    .bind(episodeIds).to("episodeIds")
                    .run();
        }
        originId.ifPresent(id -> linkLocation(dto.getId(), id, "ORIGINATED_FROM"));
        currentId.ifPresent(id -> linkLocation(dto.getId(), id, "CURRENTLY_LOCATED_AT"));
    }

    private void linkLocation(Long characterId, Long locationId, String relationship) {
        String cypher = "MATCH (character:Character {source: $source, externalId: $characterId}) "
                + "MERGE (location:Location {source: $source, externalId: $locationId}) "
                + "MERGE (character)-[:" + relationship + "]->(location)";
        neo4jClient.query(cypher)
                .bind(SOURCE).to("source")
                .bind(characterId).to("characterId")
                .bind(locationId).to("locationId")
                .run();
    }

    private <T> T read(ResourceMessage message, Class<T> type) {
        try {
            return objectMapper.treeToValue(message.getPayload(), type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid graph payload", exception);
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
}
