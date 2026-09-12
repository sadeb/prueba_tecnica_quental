# workflows/ — Orden de construcción

Cada workflow es un paso acotado: objetivo, contexto a cargar, pasos, criterio de hecho, comandos que ejecuta el **humano**, commit propuesto. El orden sigue [spec/prioridades.md](../spec/prioridades.md). Un agente carga **solo** el workflow en curso ([economia-tokens](../skills/economia-tokens/SKILL.md)).

| # | Workflow | Rol | Depende de |
|---|---|---|---|
| 00 | [Bootstrap del repositorio](00-bootstrap-repositorio.md) | infra / orquestador | — |
| 01 | [Infraestructura docker-compose](01-infraestructura-docker.md) | infra | 00 |
| 02 | [Esqueleto backend](02-backend-esqueleto.md) | backend | 00 |
| 03 | [Cliente API externa](03-cliente-api-externa.md) | backend | 02 |
| 04 | [Productor Kafka](04-productor-kafka.md) | backend | 03, 01 |
| 05 | [Consumidor Kafka](05-consumidor-kafka.md) | backend | 04, 06, 07 |
| 06 | [Persistencia PostgreSQL](06-persistencia-postgres.md) | backend | 02 |
| 07 | [Persistencia Neo4j](07-persistencia-neo4j.md) | backend | 02 |
| 08 | [Autenticación](08-autenticacion.md) | backend | 06 |
| 09 | [Favoritos](09-favoritos.md) | backend | 08 |
| 10 | [API de consulta](10-api-consulta.md) | backend | 06, 07 |
| 11 | [OpenAPI](11-openapi.md) | backend / docs | 08–10 |
| 12 | [Esqueleto frontend](12-frontend-esqueleto.md) | frontend | 00 |
| 13 | [Frontend: auth](13-frontend-auth.md) | frontend | 12, 08 |
| 14 | [Frontend: personajes](14-frontend-personajes.md) | frontend | 13, 10 |
| 15 | [Frontend: favoritos](15-frontend-favoritos.md) | frontend | 14, 09 |
| 16 | [Pruebas backend](16-pruebas-backend.md) | qa | 03–10 (incremental) |
| 17 | [Pruebas frontend](17-pruebas-frontend.md) | qa | 13–15 |
| 18 | [README y entrega](18-readme-entrega.md) | docs | todo |

Orden sugerido de ejecución: 00 → 01 → 02 → 06 → 07 → 03 → 04 → 05 → 16 (sync) → 08 → 09 → 10 → 11 → 16 (api) → 12 → 13 → 14 → 15 → 17 → 18. Bonus después ([spec/08](../spec/08-bonus.md)).

Plantilla de cada workflow: Objetivo · Contexto · Pasos · Hecho cuando · Ejecuta y pega · Commit propuesto.
