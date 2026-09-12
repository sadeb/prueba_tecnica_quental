# 10 · API de consulta de datos sincronizados

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md)

## Objetivo
Listado con filtros y paginación, detalle de personaje con relaciones, y consulta de relacionados resuelta en Neo4j.

## Contexto
[conventions/api-rest.md](../conventions/api-rest.md), [spec/05](../spec/05-api-propia.md) punto 4, [ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [references/neo4j.md](../references/neo4j.md).

## Pasos
1. DTOs: `CharacterSummary`, `CharacterDetail` (origin, location, episodes resumidos), `RelatedCharacter {character, sharedEpisodes}`, `EpisodeSummary/Detail`, `LocationSummary/Detail`.
2. `CharacterQueryService.search(filters, pageable)` con `Specification` (name `ILIKE`, status, species, gender); validación de `size ≤ 100` y enums → 400.
3. `getDetail(id)` → 404 si no existe o es `placeholder`.
4. `getRelated(id, limit)`: id interno → `externalId` → Cypher → `findByExternalIdIn` → mapear conservando orden y `sharedEpisodes`; ids sin fila se omiten con `WARN`.
5. Controladores `CharacterController`, `EpisodeController`, `LocationController` (residentes vía Postgres `location_id`).
6. Tests `@WebMvcTest` (filtro inválido 400, 404, paginación) y de servicio (orden de relacionados, omisión de ids sin fila).

## Hecho cuando
`GET /api/characters?name=rick&page=0&size=5` y `GET /api/characters/1/related` responden con datos reales tras la sync.

## Ejecuta y pega
```bash
curl -s "http://localhost:8080/api/characters/1/related?limit=3"
```

## Commit propuesto
`feat(api): expose paginated character search, detail and graph-backed related characters`
