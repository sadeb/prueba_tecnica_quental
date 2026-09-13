---
name: kafka-sync
description: Procedimiento para implementar la sincronización con Kafka 2.0.1 y spring-kafka 2.8: productor paginado, consumidor idempotente, topics, claves, DLT y trazabilidad. Cargar al trabajar en projects/backend/src/main/java/.../sync/.
---

# kafka-sync

## Antes de escribir
Leer [ADR-002](../../decisions/ADR-002-idempotencia-sync.md), [ADR-003](../../decisions/ADR-003-topics-kafka.md), [ADR-006](../../decisions/ADR-006-mensajes-irrecuperables.md) y [references/kafka-2.0.1.md](../../references/kafka-2.0.1.md).

## Productor (descarga + publicación)
1. Crear `SyncRun` (`RUNNING`), rechazar con 409 si ya hay uno en curso.
2. Por entidad en orden **locations → episodes → characters**: iterar páginas desde 1 hasta `info.pages` (o hasta `next == null`); por cada elemento válido publicar `SyncMessage` con clave `externalId`.
3. Elemento inválido: log `WARN`, contador `skipped`, continuar. Fallo de página tras reintentos: log `ERROR`, marcar run `PARTIAL`, continuar con la siguiente entidad.
4. Ejecutar en un hilo aparte (`@Async` o `TaskExecutor`) para que el endpoint responda 202 con `runId`.
5. Cerrar run con contadores (`published`, `skipped`, `failedPages`).

## Consumidor (transformación + persistencia)
1. `@KafkaListener` por topic, `String` payload; parsear + validar `schemaVersion` → `InvalidMessageException` (no reintentable).
2. Upsert Postgres por `external_id` (placeholders para referencias desconocidas, [ADR-004](../../decisions/ADR-004-consistencia-postgres-neo4j.md)).
3. Upsert Neo4j con `MERGE` ([neo4j-graph](../neo4j-graph/SKILL.md)).
4. Dejar que la excepción suba: el `DefaultErrorHandler` reintenta y publica en DLT.
5. Nunca commitear offset antes de persistir (ack por registro, `enable-auto-commit=false`).

## Configuración mínima (`application.yml`)
`spring.kafka.bootstrap-servers`, `consumer.group-id=rm-sync-consumer`, `consumer.auto-offset-reset=earliest`, `consumer.enable-auto-commit=false`, `producer.acks=all`, `sync.topics.*`, `sync.on-startup=false`.

## Pruebas
`@EmbeddedKafka` + H2: publicar → consumir → contar filas; publicar dos veces → mismos conteos; JSON corrupto → aparece en `.DLT`. Ver [testing-aislado](../testing-aislado/SKILL.md).

## Entrega
Ficheros tocados, comandos para el humano y commit propuesto ([no-run-commands](../no-run-commands/SKILL.md)).
