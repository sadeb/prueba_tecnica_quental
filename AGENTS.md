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
Ver [`.agents/conventions/estructura-repositorio.md`](.agents/conventions/estructura-repositorio.md). Resumen: `backend/` (Spring Boot), `frontend/` (Angular), `docker-compose.yml`, `README.md`.

## Estado actual
Solo existe la estructura de contexto para agentes. No hay código ni infraestructura todavía. Primer paso: [`workflows/00-bootstrap-repositorio.md`](.agents/workflows/00-bootstrap-repositorio.md).
