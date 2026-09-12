# 07 · Persistencia Neo4j

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [neo4j-graph](../skills/neo4j-graph/SKILL.md)

## Objetivo
Grafo mínimo (`Character`, `Episode`, `Location`) con constraints, upsert por `MERGE` y la consulta de personajes relacionados.

## Contexto
[references/neo4j.md](../references/neo4j.md), [ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [spec/04](../spec/04-modelo-de-datos.md).

## Pasos
1. `GraphSchemaInitializer` (`ApplicationRunner`): crea los 3 constraints únicos por `externalId` de forma idempotente.
2. `CharacterGraphService`, `EpisodeGraphService`, `LocationGraphService` con `Neo4jClient` y Cypher parametrizado: `MERGE` nodo + `SET` `name`/`code`; Character: reemplazar `ORIGIN_FROM`/`LOCATED_IN`, `MERGE` `APPEARS_IN` por cada episodio y `DELETE` de los que no estén en el snapshot.
3. `RelatedCharactersQuery`: Cypher de episodios compartidos (orden desc, límite) → lista `(externalId, sharedEpisodes)`.
4. Propiedades `spring.neo4j.*` en `application.yml`; en perfil `test`, desactivar autoconfiguración de Neo4j o usar embebido.
5. Tests unitarios del servicio de relacionados con el query mockeado; integración opcional ([references/testing-backend.md](../references/testing-backend.md)).

## Hecho cuando
Tras sync, consulta manual en Neo4j Browser devuelve relacionados de Rick (id 1) ordenados por coincidencias.

## Ejecuta y pega
```bash
docker compose exec neo4j cypher-shell -u neo4j -p "$NEO4J_PASSWORD" "MATCH (c:Character {externalId:1})-[:APPEARS_IN]->(e)<-[:APPEARS_IN]-(o) WHERE o<>c RETURN o.name, count(e) AS n ORDER BY n DESC LIMIT 5"
```

## Commit propuesto
`feat(graph): persist entity relationships in neo4j with merge-based upserts and related-characters query`
