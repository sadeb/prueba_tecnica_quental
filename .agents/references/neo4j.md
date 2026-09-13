# Neo4j 4.4 + Spring Data Neo4j 6.3

- Imagen `neo4j:4.4.48-community`. Variables: `NEO4J_AUTH=neo4j/<password>` (solo aplica en la primera inicialización del volumen), `NEO4J_dbms_memory_heap_max__size=256m`, `NEO4J_dbms_memory_pagecache_size=64m`. Puertos 7474 (browser), 7687 (bolt). Healthcheck: `wget --no-verbose --tries=1 --spider http://127.0.0.1:7474 || exit 1`.
- Propiedades Boot: `spring.neo4j.uri=bolt://neo4j:7687`, `spring.neo4j.authentication.username/password`.
- SDN 6: `@Node`, `@Id` (usar `externalId` como id de negocio; `@Id @GeneratedValue` no es necesario), `@Relationship(type="APPEARS_IN", direction=OUTGOING)`. Repositorios `Neo4jRepository<T, Long>`; consultas con `@Query` en Cypher.
- Para escritura idempotente **preferir Cypher `MERGE` explícito** (vía `Neo4jClient` o `@Query`) a `save()` del repositorio: `save()` de SDN reemplaza colecciones de relaciones y es fácil generar duplicados o borrados no deseados.

## Modelo de grafo propuesto
Nodos: `(:Character {externalId, name})`, `(:Episode {externalId, code})`, `(:Location {externalId, name})`. Solo propiedades necesarias para navegar; los atributos completos viven en Postgres.

Relaciones: `(c)-[:APPEARS_IN]->(e)`, `(c)-[:ORIGIN_FROM]->(l)`, `(c)-[:LOCATED_IN]->(l)`. Residentes = inverso de `LOCATED_IN`.

Constraints (crear al arrancar, idempotentes): `CREATE CONSTRAINT character_external_id IF NOT EXISTS ON (c:Character) ASSERT c.externalId IS UNIQUE` (sintaxis 4.x); ídem Episode y Location.

## Cypher clave
Upsert personaje con episodios:
```cypher
MERGE (c:Character {externalId: $id}) SET c.name = $name
WITH c
UNWIND $episodeIds AS eid
MERGE (e:Episode {externalId: eid})
MERGE (c)-[:APPEARS_IN]->(e)
```
Personajes relacionados por episodios comunes (consulta obligatoria del grafo):
```cypher
MATCH (c:Character {externalId: $id})-[:APPEARS_IN]->(e:Episode)<-[:APPEARS_IN]-(o:Character)
WHERE o <> c
RETURN o.externalId AS externalId, count(e) AS sharedEpisodes
ORDER BY sharedEpisodes DESC, o.externalId ASC LIMIT $limit
```
Nota: `MERGE` en relaciones ORIGIN_FROM/LOCATED_IN debe borrar la anterior si cambió: `OPTIONAL MATCH (c)-[r:LOCATED_IN]->() DELETE r` antes del `MERGE`.

## Tests
Opciones: Testcontainers `Neo4jContainer` (necesita Docker) o `neo4j-harness` 4.4 (embebido, JDK 11). Ver [testing-backend.md](testing-backend.md).

Relacionado: [spec/04-modelo-de-datos.md](../spec/04-modelo-de-datos.md), [ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [workflows/07-persistencia-neo4j.md](../workflows/07-persistencia-neo4j.md).
