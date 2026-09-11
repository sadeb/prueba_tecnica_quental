# Handoff actual

- Actualizado: 2026-09-11
- Fase: `0 - CONTEXT_BOOTSTRAP`
- Estado: `IMPLEMENTADO Y VALIDADO`

## Objetivo vigente

Mantener únicamente la estructura documental multiagente y las carpetas reservadas. No crear todavía backend, frontend, infraestructura ejecutable ni skills.

## Hecho en esta fase

- PDF de requisitos leído y revisado visualmente en sus tres páginas.
- Requisitos clasificados por procedencia.
- Contexto neutral para agentes y adaptadores mínimos preparados.
- Arquitectura objetivo y cuatro decisiones iniciales documentadas.
- Roadmap protegido mediante puertas de aprobación.
- Directorios de aplicación reservados sin scaffolds.

## Decisiones vigentes

- `AGENTS.md` es la fuente canónica.
- Java 11, Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- Spring MVC, JPA/Hibernate y Liquibase como único gestor del esquema.
- PostgreSQL como fuente de verdad, Neo4j como proyección y Kafka como canal.
- Transactional outbox para consistencia y tokens opacos propios de 256 bits.
- Angular estable, desacoplado; Bootstrap; sin Playwright.
- Todos los bonus del PDF están incluidos en el alcance futuro.

## Verificaciones

- `git diff --check`: correcto, sin errores de whitespace.
- Parseo de `.gemini/settings.json`: correcto.
- Resolución de enlaces Markdown locales: correcta.
- Adaptador `CLAUDE.md`: contiene únicamente `@AGENTS.md`.
- Búsqueda de artefactos prohibidos: no existen proyectos, `src/`, dependencias, Compose, `SKILL.md` ni locks de skills.
- Inventario del repositorio: coincide con la estructura prevista para la Fase 0.

## Siguiente paso autorizado

Solo validar y, si es necesario, corregir la Fase 0. La generación del backend Maven requiere una nueva autorización explícita del usuario.

## Riesgos abiertos

- La versión exacta de Angular se fijará al autorizar la Fase 2, tras comprobar compatibilidad con Node y TypeScript vigentes.
- Temas Kafka, payload, reintentos y DLT se concretarán por ADR antes de implementar la Fase 5.
