# ADR-001 · Identificador externo, id interno y campos ausentes

**Estado**: Propuesta · **Requisito**: [spec/04](../spec/04-modelo-de-datos.md), [spec/02](../spec/02-integracion-api-externa.md) · **Fecha**: 2026-09-12

## Contexto
La fuente asigna un `id` entero a cada recurso. El modelo interno no debe acoplarse al proveedor, pero la correspondencia Postgres ↔ Neo4j debe ser estable y reproducible entre sincronizaciones. La API tiene campos vacíos (`""`) y referencias sin id (`origin.url == ""`).

## Decisión
- Cada entidad sincronizada tiene **id interno** (`bigserial`, PK) y **`external_id`** (`bigint NOT NULL UNIQUE`). La API propia expone ambos; las rutas usan el id interno.
- La **clave de correspondencia** entre almacenes es `external_id`: los nodos Neo4j tienen `externalId` con constraint único. Nunca se usa el id interno en el grafo.
- Los upserts se hacen por `external_id` (`ON CONFLICT (external_id) DO UPDATE` en Postgres; `MERGE {externalId}` en Neo4j).
- Campos de texto vacíos (`type`, `dimension`, `species`...) se **normalizan a `null`** en el mapeador externo → dominio; la API propia devuelve `null`, el frontend muestra "—".
- Referencias sin URL (`origin`/`location` = unknown) → columna `origin_id`/`location_id` **nullable**; no se crea Location ficticia.
- Ids extraídos de URLs que no sean enteros → el elemento se marca inválido y se rechaza con traza (`InvalidExternalPayloadException`), nunca se persiste parcialmente.
- Se conserva `source = 'rickandmortyapi'` en una columna solo si se prevé más de una fuente: **no** se añade (YAGNI).

## Alternativas descartadas
- Usar el id externo como PK: más simple, pero acopla claves de la API propia al proveedor y complica un futuro cambio de fuente.
- UUID interno: no aporta frente a `bigserial` en un sistema de un solo nodo.

## Consecuencias
Mapeo determinista y reejecutable; una columna extra por tabla; los tests de idempotencia se apoyan en la unicidad de `external_id`.

## Dónde se aplica
[wf 03](../workflows/03-cliente-api-externa.md), [wf 06](../workflows/06-persistencia-postgres.md), [wf 07](../workflows/07-persistencia-neo4j.md). Relacionado: [ADR-002](ADR-002-idempotencia-sync.md).
