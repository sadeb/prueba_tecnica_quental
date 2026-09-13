# AGENTS.md — Prueba técnica Full Stack (Java / Angular) · Quental

Punto de entrada único para cualquier agente de IA. Lee esto primero; el resto está en [`.agents/`](.agents/README.md) segmentado por tema.

## Qué es este proyecto
Aplicación que consume la API pública de Rick and Morty, la publica en Kafka, la persiste en PostgreSQL (atributos + usuarios) y Neo4j (grafo de relaciones), expone una API REST propia con OpenAPI, y una SPA Angular + Bootstrap que consume solo esa API. Se evalúa como prueba técnica: criterio de diseño, calidad de código, dominio del stack y capacidad de justificar cada decisión.

Detalle completo: [`.agents/spec/`](.agents/spec/README.md). Prioridad si falta tiempo: [`.agents/spec/prioridades.md`](.agents/spec/prioridades.md).

## Stack fijado (no negociable)
JDK 11 · Spring Boot 2.7.x · PostgreSQL 10 · Neo4j · Kafka 2.12-2.0.1 · Angular + Bootstrap · Docker Compose. Ver [`.agents/references/stack-versiones.md`](.agents/references/stack-versiones.md).

## Reglas obligatorias para todo agente
1. **Nunca ejecutar `git commit`, `git push` ni comandos que alteren el historial.** Skill: [`no-git-write`](.agents/skills/no-git-write/SKILL.md).
2. **Nunca arrancar backend, frontend, docker compose ni tests.** El humano los ejecuta y pega la salida. Skill: [`no-run-commands`](.agents/skills/no-run-commands/SKILL.md).
3. **Economía de tokens**: lee solo los ficheros del tema en curso; no cargues todo `.agents/`. Skill: [`economia-tokens`](.agents/skills/economia-tokens/SKILL.md).
4. **Economía de dependencias**: resolver con el framework antes que añadir librerías. Cada dependencia nueva exige un ADR en [`.agents/decisions/`](.agents/decisions/README.md).
5. Toda decisión de diseño relevante queda registrada como ADR breve; se defenderá en entrevista.
6. Seguir las convenciones de [`.agents/conventions/`](.agents/conventions/README.md).

## Cómo trabajar
- Elige el rol adecuado en [`.agents/agents/`](.agents/agents/README.md).
- Sigue el workflow correspondiente en [`.agents/workflows/`](.agents/workflows/README.md); cada workflow enlaza la spec, referencias y ADRs que necesita.
- Consulta términos en [`.agents/glossary.md`](.agents/glossary.md).

## Estructura prevista del repositorio
Ver [`.agents/conventions/estructura-repositorio.md`](.agents/conventions/estructura-repositorio.md). Resumen: `projects/backend/` (Spring Boot), `projects/frontend/` (Angular), `projects/docker-compose.yml` + `projects/.env`, `README.md` raíz.

## Estado actual
Contexto de agentes e infraestructura Docker hechos ([workflows/00](.agents/workflows/00-bootstrap-repositorio.md), [01](.agents/workflows/01-infraestructura-docker.md), [ADR-008](.agents/decisions/ADR-008-imagenes-docker.md)). **Backend completo** en `projects/backend/` ([workflows/02–11 y 16](.agents/workflows/README.md)): cliente externo, productor/consumidor Kafka con DLT, Flyway + JPA, Neo4j, auth con token propio, favoritos, API de consulta, OpenAPI activable ([ADR-009](.agents/decisions/ADR-009-openapi-toggle.md)) y tests (unitarios, slices y `SyncFlowIT`). Fundamentación de cada decisión y revisiones por paso: [docs/decisiones-tecnicas-backend.md](docs/decisiones-tecnicas-backend.md). Pendiente de verificación por el humano (`./mvnw test`, arranque en compose). Siguiente paso: [`workflows/12-frontend-esqueleto.md`](.agents/workflows/12-frontend-esqueleto.md).
