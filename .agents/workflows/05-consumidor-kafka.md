# 05 · Consumidor Kafka (transformación + persistencia)

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [kafka-sync](../skills/kafka-sync/SKILL.md), [neo4j-graph](../skills/neo4j-graph/SKILL.md)

## Objetivo
Listeners que consumen los tres topics, hacen upsert idempotente en PostgreSQL y Neo4j, y envían mensajes irrecuperables al DLT sin bloquear.

## Contexto
[ADR-002](../decisions/ADR-002-idempotencia-sync.md), [ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [ADR-006](../decisions/ADR-006-mensajes-irrecuperables.md), [references/kafka-2.0.1.md](../references/kafka-2.0.1.md). Requiere [06](06-persistencia-postgres.md) y [07](07-persistencia-neo4j.md).

## Pasos
1. `KafkaConsumerConfig`: `ConcurrentKafkaListenerContainerFactory` con `DefaultErrorHandler(DeadLetterPublishingRecoverer(kafkaTemplate), FixedBackOff(1000, 2))`, `addNotRetryableExceptions(InvalidMessageException.class)`, ack manual por registro.
2. `SyncMessageParser`: `String` → `SyncMessage`; valida `schemaVersion` y `entityType`; errores → `InvalidMessageException`.
3. `LocationConsumer`, `EpisodeConsumer`, `CharacterConsumer` (`@KafkaListener`, `groupId = rm-sync-consumer`): parsear → `XxxPersistenceService.upsert(snapshot)` (Postgres, con placeholders) → `XxxGraphService.upsert(snapshot)` (Neo4j) → ack.
4. Contador `failed_messages` en `sync_runs` al publicar en DLT (listener del recoverer o `ConsumerRecordRecoverer` propio que delega).
5. Logs: `INFO` por mensaje persistido a nivel `DEBUG`, `INFO` cada 100 mensajes, `ERROR` en DLT con topic/clave/causa.

## Hecho cuando
Tras una sync completa: `characters` = 826, `episodes` = 51, `locations` = 126 en Postgres; mismos conteos de nodos en Neo4j; segunda sync deja los mismos conteos.

## Ejecuta y pega
```bash
cd projects && docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select (select count(*) from characters) c, (select count(*) from episodes) e, (select count(*) from locations) l, (select count(*) from character_episodes) ce;"
```
```bash
cd projects && docker compose exec neo4j cypher-shell -u neo4j -p "$NEO4J_PASSWORD" "MATCH (n) RETURN labels(n)[0] AS label, count(*) AS n"
```
Pegar ambas tablas, antes y después de una segunda sync.

## Commit propuesto
`feat(sync): consume kafka topics with idempotent upserts into postgres and neo4j and dead-letter handling`
