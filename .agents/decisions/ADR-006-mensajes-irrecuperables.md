# ADR-006 · Dead Letter Topic y registro de mensajes procesados

**Estado**: Propuesta · **Requisito**: [spec/03](../spec/03-sincronizacion-kafka.md) punto 4; bonus B3 ([spec/08](../spec/08-bonus.md)) · **Fecha**: 2026-09-12

## Contexto
Un mensaje defectuoso no puede bloquear el consumo ni dejar el conjunto a medias sin traza. Los fallos pueden ser transitorios (BD caída) o permanentes (JSON inválido, referencia imposible).

## Decisión
- `DefaultErrorHandler` de spring-kafka con `FixedBackOff(1000 ms, 2 reintentos)` → tras 3 intentos, `DeadLetterPublishingRecoverer` publica el registro original en `<topic>.DLT` con cabeceras de excepción (las añade spring-kafka) y el consumidor avanza. Ver [references/kafka-2.0.1.md](../references/kafka-2.0.1.md).
- Los errores de **deserialización/validación** (JSON inválido, `schemaVersion` desconocido, payload que no pasa validación) se clasifican como **no reintentables** (`addNotRetryableExceptions(InvalidMessageException.class)`) y van al DLT al primer intento.
- Cada envío a DLT incrementa `sync_runs.failed_messages` y deja un `ERROR` en log con topic, clave y causa. Es la "traza" exigida.
- **Registro de mensajes procesados** (`processed_messages(topic, partition, offset, processed_at)`, PK compuesta): bonus opcional. Se implementa **solo** si sobra tiempo; la idempotencia no depende de él ([ADR-002](ADR-002-idempotencia-sync.md)). Si se implementa, se inserta en la misma transacción que el upsert de Postgres y se consulta antes de procesar para saltar duplicados exactos (ahorro, no corrección).
- Reproceso del DLT: fuera de alcance; documentar en README cómo inspeccionar `rm.*.DLT` con las herramientas de consola de Kafka.

## Alternativas descartadas
- Reintentos infinitos: bloquean el consumo (prohibido por el enunciado).
- Descartar sin publicar en DLT: pierde el mensaje sin posibilidad de análisis.
- Retry topics con `@RetryableTopic`: más infraestructura de la necesaria.

## Consecuencias
Un topic DLT por entidad; el flujo nunca se bloquea; los fallos son visibles en `sync_runs` y en logs. Test: mensaje corrupto → aparece en DLT y el siguiente mensaje válido se procesa.

## Dónde se aplica
[wf 05](../workflows/05-consumidor-kafka.md), [wf 16](../workflows/16-pruebas-backend.md).
