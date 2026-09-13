# 04 · Productor Kafka (descarga + publicación)

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [kafka-sync](../skills/kafka-sync/SKILL.md)

## Objetivo
Sincronización lanzable por `POST /api/admin/sync` que recorre la fuente paginada y publica snapshots en `rm.locations`, `rm.episodes`, `rm.characters`.

## Contexto
[spec/03](../spec/03-sincronizacion-kafka.md), [ADR-002](../decisions/ADR-002-idempotencia-sync.md), [ADR-003](../decisions/ADR-003-topics-kafka.md), [references/kafka-2.0.1.md](../references/kafka-2.0.1.md), [conventions/api-rest.md](../conventions/api-rest.md).

## Pasos
1. `SyncTopicsProperties` + beans `NewTopic` (3 topics + 3 DLT, 1 partición, RF 1).
2. `SyncMessage` (envoltorio de ADR-003) y `SyncMessageSerializer` (Jackson → `String`).
3. Entidad/tabla `sync_runs` (changeset Liquibase `002-sync-runs.sql`; ver [06](06-persistencia-postgres.md)) con estado y contadores.
4. `SyncProducerService`: crea run, itera entidades en orden, publica por clave `externalId`, cuenta `published`/`skipped`/`failedPages`, cierra run (`COMPLETED`/`PARTIAL`/`FAILED`). Ejecución asíncrona.
5. `SyncAdminController`: `POST /api/admin/sync` → 202 `{runId}`; 409 si hay run `RUNNING`; solo rol `ADMIN` ([ADR-012](../decisions/ADR-012-administrador-sistema.md)). `GET /api/admin/sync/{runId}`.
6. Propiedad `sync.on-startup` (default `false`) con `ApplicationRunner` opcional.
7. Bonus B2 (después de lo obligatorio): tabla `raw_payloads` con upsert del JSON crudo por `(entity_type, external_id)`.

## Hecho cuando
Con la infra levantada, el humano lanza la sync y ve en logs `published=` por entidad; los topics existen.

## Ejecuta y pega
```bash
curl -s -X POST http://localhost:8080/api/admin/sync
```
Pegar la respuesta JSON y las líneas de log que contengan `sync run`.

## Commit propuesto
`feat(sync): publish external snapshots to kafka topics via admin-triggered sync run`
