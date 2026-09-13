# 18 · README y entrega

**Rol**: [redactor-docs](../agents/redactor-docs.md)

## Objetivo
README raíz completo y verificado, ADRs en estado `Aceptada`, repositorio listo para evaluación.

## Contexto
[spec/10](../spec/10-entrega.md), [decisions/](../decisions/README.md), [references/docker-compose.md](../references/docker-compose.md), [conventions/api-rest.md](../conventions/api-rest.md).

## Secciones del README (en este orden)
1. Descripción en 3 líneas y diagrama de flujo textual (fuente → productor → Kafka → consumidor → Postgres/Neo4j → API → Angular).
2. Requisitos previos (Docker, opcionalmente JDK 11 y Node para desarrollo).
3. Arranque con `cd projects && docker compose up --build` y URLs (frontend, API, Swagger, Neo4j Browser).
4. Lanzar la sincronización (`POST /api/admin/sync` con token) y comprobar estado.
5. Desarrollo local: backend fuera de Docker, frontend con `ng serve` y proxy.
6. Tests: comandos backend y frontend; qué prueba `SyncFlowIT`.
7. Decisiones de diseño: un párrafo por ADR con enlace a `.agents/decisions/ADR-xxx.md`.
8. Limitaciones conocidas y qué se haría con más tiempo.
9. Estructura del repositorio.

## Pasos
1. Redactar con frases cortas y comandos en bloques.
2. Pasar ADRs a `Aceptada` con el humano.
3. Comprobación por el humano desde cero: `cd projects && docker compose down -v && docker compose up --build`, seguir el README literalmente.
4. Revisión final con [revisor-codigo](../agents/revisor-codigo.md) sobre los criterios de [spec/09](../spec/09-criterios-valoracion.md).

## Hecho cuando
Un evaluador sin contexto levanta el proyecto siguiendo solo el README.

## Ejecuta y pega
```bash
cd projects && docker compose down -v && docker compose up --build -d && sleep 60 && docker compose ps
```

## Commit propuesto
`docs: add setup, execution and design decisions to README`
