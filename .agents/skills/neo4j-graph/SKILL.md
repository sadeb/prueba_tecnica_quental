---
name: neo4j-graph
description: Procedimiento para el grafo en Neo4j 4.4 con Spring Data Neo4j 6: nodos mínimos, constraints, upsert con MERGE, borrado de relaciones obsoletas y la consulta de personajes relacionados. Cargar al trabajar en graph/ o en los *GraphRepository.
---

# neo4j-graph

## Antes de escribir
Leer [references/neo4j.md](../../references/neo4j.md) y [ADR-004](../../decisions/ADR-004-consistencia-postgres-neo4j.md).

## Reglas
- El grafo solo guarda `externalId` (+ `name`, `code`) y relaciones. Atributos completos → Postgres.
- Escrituras con **Cypher `MERGE` explícito** vía `Neo4jClient` o `@Query`, nunca `save()` de entidades con colecciones de relaciones (riesgo de duplicar/borrar).
- Constraints únicos por `externalId` creados al arrancar de forma idempotente (`IF NOT EXISTS`, sintaxis 4.x).
- Relaciones 1:1 (`ORIGIN_FROM`, `LOCATED_IN`): borrar la existente antes del `MERGE` de la nueva. Relaciones N:M (`APPEARS_IN`): `MERGE` de las del snapshot y `DELETE` de las que no estén en la lista (`WHERE NOT e.externalId IN $episodeIds`).
- Parámetros siempre (`$id`), nunca concatenar Cypher.
- Una transacción de Neo4j por mensaje (`@Transactional(transactionManager = "neo4jTransactionManager")` o transacción del `Neo4jClient`); independiente de la de JPA ([ADR-004](../../decisions/ADR-004-consistencia-postgres-neo4j.md)).

## Consulta obligatoria
`GET /api/characters/{id}/related` → Cypher de [references/neo4j.md](../../references/neo4j.md) (episodios compartidos, orden desc por coincidencias, límite). El servicio recibe `[externalId, sharedEpisodes]` y completa atributos desde Postgres en una sola consulta `findByExternalIdIn`.

## Pruebas
Unitarias del servicio con el repositorio de grafo mockeado (orden, límite, ids sin fila en Postgres). Integración opcional con `neo4j-harness` 4.4 o Testcontainers ([references/testing-backend.md](../../references/testing-backend.md)).

## Entrega
Ficheros tocados, Cypher final pegado en el ADR si cambió, comandos para el humano ([no-run-commands](../no-run-commands/SKILL.md)).
