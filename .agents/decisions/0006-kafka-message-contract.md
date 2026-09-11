# ADR 0006: Contrato de mensajería Kafka

- Estado: Aceptado e implementado
- Fecha: 2026-09-11
- Alcance: sincronización y proyección Neo4j

## Contexto

La descarga externa, la persistencia relacional y la proyección de grafo deben permanecer desacopladas, soportar entregas repetidas y dejar los fallos parciales trazables. Kafka 2.0.1 limita la selección de clientes y obliga a evitar funciones modernas del broker.

## Decisión

- Usar `rickmorty.raw.v1` para recursos validados pendientes de persistencia y `rickmorty.graph.v1` para eventos de proyección Neo4j.
- Usar como clave el identificador externo decimal. El payload incluye `messageId`, `syncRunId`, `resourceType`, `externalId`, `schemaVersion` y el JSON del recurso.
- Mantener `schemaVersion = 1` y añadir campos compatibles hacia atrás; un cambio incompatible requiere un nuevo tema o versión.
- Persistir cada publicación en `outbox_events` dentro de la misma transacción PostgreSQL que origina el evento. El publicador marca el evento solo después de la confirmación del broker.
- Deduplicar el consumidor relacional por `messageId`, aplicar upsert por `externalId` y proyectar en Neo4j mediante `MERGE`.
- Configurar tres reintentos (cuatro entregas en total) con backoff fijo de un segundo. Tras agotarlos, publicar en el tema original con sufijo `.DLT`; el consumidor DLT actualiza el `sync_run` asociado.
- Usar JSON legible y claves estables, sin serialización específica de Java, para facilitar diagnóstico y evolución.

## Consecuencias

- La entrega es al menos una vez y la convergencia depende de idempotencia, no de exactamente una vez distribuido.
- Los datos confirmados en PostgreSQL sobreviven a caídas de Kafka o Neo4j y quedan reintentables en el outbox o DLT.
- Los temas deben conservar el orden por identificador externo, pero no se depende de orden global.
- Los mensajes nunca contienen usuarios, credenciales ni tokens.
