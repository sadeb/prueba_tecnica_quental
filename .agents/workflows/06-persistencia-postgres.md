# 06 · Persistencia PostgreSQL

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [spring-boot-jdk11](../skills/spring-boot-jdk11/SKILL.md)

## Objetivo
Esquema relacional versionado con Flyway, entidades JPA y servicios de upsert idempotente por `external_id`.

## Contexto
[spec/04](../spec/04-modelo-de-datos.md), [ADR-001](../decisions/ADR-001-identificador-externo.md), [ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [references/postgresql-10.md](../references/postgresql-10.md), [conventions/nomenclatura.md](../conventions/nomenclatura.md).

## Pasos
1. `V1__init.sql`: `locations(id, external_id UNIQUE, name, type, dimension, placeholder, created_at, updated_at)`, `episodes(id, external_id UNIQUE, name, air_date, code, placeholder, …)`, `characters(id, external_id UNIQUE, name, status, species, type, gender, image_url, origin_id FK NULL, location_id FK NULL, placeholder, …)`, `character_episodes(character_id, episode_id, PK compuesta)`. Índices en `characters(name)`, `(status)`, `(species)`, `(gender)`.
2. Entidades JPA (`Character`, `Episode`, `Location`) con `@ManyToOne` a Location y `@ManyToMany` a Episode (o entidad explícita de unión si simplifica el reemplazo). Enums `Status`, `Gender` con valor `UNKNOWN`.
3. Repositorios Spring Data: `findByExternalId`, `findByExternalIdIn`, `Specification`/query methods para filtros de [api-rest](../conventions/api-rest.md).
4. `XxxPersistenceService.upsert(snapshot)`: buscar por `external_id`; crear o actualizar; `placeholder=false`; para Character resolver `origin`/`location` (crear placeholder si no existe, `null` si el snapshot no trae id) y reemplazar el conjunto de episodios (crear placeholders para ids desconocidos). Todo en una `@Transactional`.
5. `@DataJpaTest` con H2 modo Postgres: upsert dos veces → 1 fila; placeholder completado; `origin` nulo.

## Hecho cuando
Flyway aplica `V1` al arrancar; tests del paso en verde (humano).

## Ejecuta y pega
```bash
cd backend && ./mvnw -q test -Dtest='*PersistenceServiceTest,*RepositoryTest' && echo TESTS_OK
```

## Commit propuesto
`feat(db): add flyway schema and idempotent jpa upserts keyed by external id`
