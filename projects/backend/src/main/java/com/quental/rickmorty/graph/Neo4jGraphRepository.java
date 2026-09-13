package com.quental.rickmorty.graph;

import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.LocationSnapshot;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graph writes are explicit Cypher MERGEs keyed by externalId (ADR-001, references/neo4j.md), never
 * SDN save() of entities with relationship collections. Each upsert is ONE statement, so it is atomic
 * in its own auto-commit transaction; Postgres has already committed when it runs (ADR-004).
 * Nodes keep only what navigation needs (externalId, name, code); attributes live in PostgreSQL.
 */
@Repository
public class Neo4jGraphRepository implements GraphRepository {

    private static final List<String> CONSTRAINTS = List.of(
            "CREATE CONSTRAINT character_external_id IF NOT EXISTS ON (c:Character) ASSERT c.externalId IS UNIQUE",
            "CREATE CONSTRAINT episode_external_id IF NOT EXISTS ON (e:Episode) ASSERT e.externalId IS UNIQUE",
            "CREATE CONSTRAINT location_external_id IF NOT EXISTS ON (l:Location) ASSERT l.externalId IS UNIQUE");

    private static final String UPSERT_LOCATION =
            "MERGE (l:Location {externalId: $id}) SET l.name = $name";

    private static final String UPSERT_EPISODE =
            "MERGE (e:Episode {externalId: $id}) SET e.name = $name, e.code = $code";

    // FOREACH over a 0/1-element list is the Cypher idiom for a conditional MERGE (null reference = no relation).
    private static final String UPSERT_CHARACTER =
            "MERGE (c:Character {externalId: $id}) SET c.name = $name "
            + "WITH c "
            + "OPTIONAL MATCH (c)-[old:ORIGIN_FROM|LOCATED_IN]->() DELETE old "
            + "WITH DISTINCT c "
            + "FOREACH (oid IN CASE WHEN $originId IS NULL THEN [] ELSE [$originId] END | "
            + "  MERGE (o:Location {externalId: oid}) MERGE (c)-[:ORIGIN_FROM]->(o)) "
            + "FOREACH (lid IN CASE WHEN $locationId IS NULL THEN [] ELSE [$locationId] END | "
            + "  MERGE (l:Location {externalId: lid}) MERGE (c)-[:LOCATED_IN]->(l)) "
            + "WITH c "
            + "OPTIONAL MATCH (c)-[gone:APPEARS_IN]->(e:Episode) WHERE NOT e.externalId IN $episodeIds DELETE gone "
            + "WITH DISTINCT c "
            + "FOREACH (eid IN $episodeIds | MERGE (e:Episode {externalId: eid}) MERGE (c)-[:APPEARS_IN]->(e))";

    private static final String RELATED =
            "MATCH (c:Character {externalId: $id})-[:APPEARS_IN]->(e:Episode)<-[:APPEARS_IN]-(o:Character) "
            + "WHERE o <> c "
            + "RETURN o.externalId AS externalId, count(e) AS sharedEpisodes "
            + "ORDER BY sharedEpisodes DESC, o.externalId ASC LIMIT $limit";

    private final Neo4jClient client;

    public Neo4jGraphRepository(Neo4jClient client) {
        this.client = client;
    }

    @Override
    public void ensureConstraints() {
        for (String cypher : CONSTRAINTS) {
            client.query(cypher).run();
        }
    }

    @Override
    public void upsertLocation(LocationSnapshot snapshot) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", snapshot.getExternalId());
        params.put("name", snapshot.getName());
        client.query(UPSERT_LOCATION).bindAll(params).run();
    }

    @Override
    public void upsertEpisode(EpisodeSnapshot snapshot) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", snapshot.getExternalId());
        params.put("name", snapshot.getName());
        params.put("code", snapshot.getCode());
        client.query(UPSERT_EPISODE).bindAll(params).run();
    }

    @Override
    public void upsertCharacter(CharacterSnapshot snapshot) {
        // HashMap, not Map.of: origin/location may be null and Map.of rejects null values.
        Map<String, Object> params = new HashMap<>();
        params.put("id", snapshot.getExternalId());
        params.put("name", snapshot.getName());
        params.put("originId", snapshot.getOriginExternalId());
        params.put("locationId", snapshot.getLocationExternalId());
        params.put("episodeIds", new ArrayList<>(snapshot.getEpisodeExternalIds()));
        client.query(UPSERT_CHARACTER).bindAll(params).run();
    }

    @Override
    public List<RelatedCharacter> findRelated(long externalId, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", externalId);
        params.put("limit", limit);
        return new ArrayList<>(client.query(RELATED)
                .bindAll(params)
                .fetchAs(RelatedCharacter.class)
                .mappedBy((typeSystem, row) -> new RelatedCharacter(
                        row.get("externalId").asLong(),
                        row.get("sharedEpisodes").asLong()))
                .all());
    }
}
