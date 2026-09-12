# ADR-004 · Coherencia entre PostgreSQL y Neo4j ante fallos parciales

**Estado**: Propuesta · **Requisito**: [spec/04](../spec/04-modelo-de-datos.md) · **Fecha**: 2026-09-12

## Contexto
No hay transacción distribuida entre JPA y Neo4j. Un mensaje puede persistirse en Postgres y fallar en Neo4j (o viceversa). Los mensajes pueden llegar antes que las entidades que referencian. No se pide alta disponibilidad: se busca la estrategia más simple y defendible.

## Decisión
- **Postgres primero, Neo4j después**, dentro del procesamiento de un mismo mensaje. Si Postgres falla → excepción, sin commit de offset, reintento ([ADR-006](ADR-006-mensajes-irrecuperables.md)). Si Neo4j falla tras el commit de Postgres → excepción, reintento del mensaje completo: el upsert en Postgres es idempotente ([ADR-002](ADR-002-idempotencia-sync.md)), así que reprocesar es seguro y **la reconciliación es el propio reintento**.
- Tras agotar reintentos el mensaje va al DLT con traza; `sync_run` registra `failed_messages`. La API de estado de la sync expone el desfase para que sea visible, no silencioso.
- **Referencias adelantadas**: si un Character referencia una Location o Episode aún no persistido, el consumidor crea un **placeholder** mínimo (`external_id` + `name` si viene en el snapshot, resto `null`, flag `placeholder = true`) en Postgres y un nodo `MERGE {externalId}` en Neo4j. Cuando llega el snapshot real, el upsert completa la fila y pone `placeholder = false`. Esto evita depender del orden de publicación.
- Neo4j guarda **solo** `externalId` y `name` (y `code` en Episode): la API compone la respuesta de relacionados leyendo ids del grafo y atributos de Postgres. Una fila inexistente en Postgres para un id del grafo se trata como "desfase pendiente" y se omite del listado con `WARN`.
- Comando de reparación: `POST /api/admin/sync` reejecuta todo; al ser idempotente, repara cualquier desfase. No se implementa reconciliación incremental.

## Alternativas descartadas
- Outbox pattern / saga: correcto pero sobreingeniería para el alcance.
- Escribir Neo4j primero: Postgres es fuente de verdad; preferimos que el grafo sea lo que quede "atrás".
- Rechazar mensajes con referencias desconocidas: bloquea el consumo por orden de llegada.

## Consecuencias
Ventanas breves de inconsistencia visibles y reparables con una reejecución; código simple. Test: fallo simulado en Neo4j → mensaje reintentado → estado final coherente.

## Dónde se aplica
[wf 05](../workflows/05-consumidor-kafka.md), [wf 06](../workflows/06-persistencia-postgres.md), [wf 07](../workflows/07-persistencia-neo4j.md), [wf 10](../workflows/10-api-consulta.md).
