# ADR 0003: Transactional outbox para propagación fiable

- Estado: Aceptado e implementado
- Fecha: 2026-09-11
- Alcance de implementación: Fases 4 y 5

## Contexto

La solución debe resistir la entrega al menos una vez de Kafka y fallos parciales entre PostgreSQL y Neo4j. Una transacción distribuida entre ambos almacenes y el broker sería desproporcionada para la prueba.

## Decisión

- Usar PostgreSQL como fuente de verdad y registrar eventos outbox en la misma transacción que el cambio que representan.
- En la ingesta, conservar el payload crudo y crear el evento de publicación de forma atómica antes de procesarlo.
- En el consumidor de dominio, aplicar upsert de entidades, registrar el identificador procesado y crear eventos para la proyección Neo4j en una sola transacción PostgreSQL.
- Publicar eventos outbox de forma reintentable en Kafka y marcarlos solo tras confirmación del broker.
- Hacer consumidores idempotentes mediante identificador único de mensaje, claves externas estables, upsert y `MERGE` en Neo4j.
- Enviar eventos no recuperables a DLT y conservar su relación con `sync_run`.

Los nombres de temas, esquema del payload, reintentos y backoff se concretan en ADR-0006.

## Consecuencias

- Una caída de Kafka o Neo4j deja trabajo pendiente recuperable sin perder el commit PostgreSQL.
- Puede haber consistencia eventual y publicaciones duplicadas; por ello la idempotencia es obligatoria.
- Se necesitan limpieza y observabilidad del outbox y de la DLT.
- Se evita introducir un coordinador de transacciones distribuidas.
