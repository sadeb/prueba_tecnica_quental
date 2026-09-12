# ADR-002 · Idempotencia extremo a extremo de la sincronización

**Estado**: Propuesta · **Requisito**: [spec/03](../spec/03-sincronizacion-kafka.md) · **Fecha**: 2026-09-12

## Contexto
Kafka entrega al menos una vez. Reejecutar la sync completa o reprocesar un mensaje no puede duplicar filas ni corromper relaciones. Debe funcionar con la paginación y con fallos parciales.

## Decisión
- **Idempotencia por diseño del estado final, no por deduplicación de mensajes**: cada mensaje describe el estado completo de una entidad (snapshot), y el consumidor hace **upsert** por `external_id` ([ADR-001](ADR-001-identificador-externo.md)). Procesar dos veces el mismo snapshot deja el mismo resultado.
- Relaciones N:M (`character_episodes`): se **reemplazan** por el conjunto del snapshot dentro de la misma transacción (delete + insert por PK compuesta). En Neo4j, `MERGE` de relaciones y borrado de las que ya no estén.
- Cada ejecución tiene un `sync_run` (`id`, `started_at`, `finished_at`, `status`, contadores). Los mensajes llevan `syncRunId` para trazabilidad, no para descartar.
- Orden de publicación en el productor: **locations → episodes → characters**, para minimizar referencias a entidades aún no vistas. El consumidor **no** depende de ese orden: referencia ausente → placeholder ([ADR-004](ADR-004-consistencia-postgres-neo4j.md)).
- Bonus B2: el productor guarda el JSON crudo en `raw_payloads (entity_type, external_id, payload jsonb, sync_run_id, fetched_at)` con upsert por `(entity_type, external_id)`; permite `POST /api/admin/sync?source=raw` que republica sin descargar. Implementar solo tras lo obligatorio.
- Registro de mensajes procesados (`processed_messages`) es **opcional** ([ADR-006](ADR-006-mensajes-irrecuperables.md)); no es necesario para la idempotencia.

## Alternativas descartadas
- Deduplicar por offset/`messageId` como único mecanismo: frágil ante reejecuciones completas (mismos datos, nuevos offsets).
- Mensajes de tipo delta/evento: obliga a ordenar y complica el reproceso; no lo pide el alcance.
- Transacciones Kafka exactly-once: sobreingeniería para un solo consumidor y el broker 2.0.1.

## Consecuencias
Consumidor simple y reejecutable; un `DELETE`+`INSERT` por personaje en la tabla N:M (aceptable para 826 personajes). Test obligatorio: consumir el mismo mensaje dos veces → mismos conteos.

## Dónde se aplica
[wf 04](../workflows/04-productor-kafka.md), [wf 05](../workflows/05-consumidor-kafka.md), [wf 16](../workflows/16-pruebas-backend.md).
