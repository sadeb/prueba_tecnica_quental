# ADR-003 · Topics, claves de mensaje y formato de carga útil

**Estado**: Propuesta · **Requisito**: [spec/03](../spec/03-sincronizacion-kafka.md) punto 5 · **Fecha**: 2026-09-12

## Contexto
Hay que justificar el diseño de topics, claves y payload, con uso proporcionado de la mensajería (sin sobreingeniería). Broker 2.0.1 ([references/kafka-2.0.1.md](../references/kafka-2.0.1.md)).

## Decisión
- **Un topic por entidad**: `rm.characters`, `rm.episodes`, `rm.locations`. Permite consumidores y tipos de mensaje distintos por entidad, y un DLT por topic (`rm.<x>.DLT`, [ADR-006](ADR-006-mensajes-irrecuperables.md)).
- **1 partición, RF 1**, creados por la app con `NewTopic`. Sin necesidad de paralelismo; orden total por topic simplifica razonar.
- **Clave** = `external_id` como `String`. Garantiza que todos los snapshots de una misma entidad caen en la misma partición y en orden, y habilita compactación si se quisiera.
- **Payload JSON** (Jackson, `String` serializer) con envoltorio:
  ```json
  { "schemaVersion": 1, "syncRunId": "…", "entityType": "CHARACTER", "externalId": 1,
    "fetchedAt": "2026-09-12T10:00:00Z", "data": { …snapshot normalizado en modelo propio… } }
  ```
  `data` es el **modelo propio ya validado y mapeado** ([spec/02](../spec/02-integracion-api-externa.md)), no el JSON del proveedor: el consumidor no conoce el formato externo.
- Cabeceras Kafka: ninguna obligatoria; `schemaVersion` va en el cuerpo para que el DLT lo conserve.
- Consumer group único `rm-sync-consumer`, `auto-offset-reset=earliest`, commit manual por registro tras persistir.
- Productor: `acks=all`, idempotencia por defecto de los clients 3.x si el broker la acepta (verificar; ver referencia).

## Alternativas descartadas
- Un topic único con campo `entityType`: mezcla ciclos de vida y obliga a un DLT compartido; menos claro.
- Publicar el JSON crudo del proveedor: acopla el consumidor al formato externo (viola req. 1).
- Avro/Schema Registry: infraestructura extra no pedida.

## Consecuencias
Tres topics + tres DLT; contrato de mensaje versionado y testeable en aislamiento. Cambiar `data` exige subir `schemaVersion`.

## Dónde se aplica
[wf 04](../workflows/04-productor-kafka.md), [wf 05](../workflows/05-consumidor-kafka.md). Nombres: [conventions/nomenclatura.md](../conventions/nomenclatura.md).
