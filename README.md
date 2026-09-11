# Prueba técnica Full Stack - Quental

Repositorio de trabajo para la prueba técnica Full Stack Java/Angular basada en la API pública de Rick and Morty.

## Estado actual

El repositorio se encuentra en la fase `CONTEXT_BOOTSTRAP`. En esta fase solo existe la estructura documental y el contexto compartido para agentes de IA. Los proyectos backend y frontend, la infraestructura ejecutable, las dependencias y las skills todavía no se han creado.

## Contexto para agentes

La fuente canónica de instrucciones es [AGENTS.md](AGENTS.md). El contexto detallado está organizado en [.agents/](.agents/README.md):

- [Requisitos](.agents/context/REQUIREMENTS.md)
- [Arquitectura planificada](.agents/context/ARCHITECTURE.md)
- [Stack objetivo](.agents/context/STACK.md)
- [Contrato futuro de API](.agents/context/API_CONTRACT.md)
- [Roadmap y puertas de aprobación](.agents/context/ROADMAP.md)
- [Handoff entre agentes](.agents/context/HANDOFF.md)
- [Skills candidatas](.agents/context/SKILLS_CANDIDATES.md)
- [Decisiones arquitectónicas](.agents/decisions/)

Claude carga las instrucciones mediante [CLAUDE.md](CLAUDE.md), y Gemini queda configurado mediante [.gemini/settings.json](.gemini/settings.json). Codex, OpenCode y cualquier agente compatible con `AGENTS.md` pueden leerlas directamente.

## Directorios reservados

- [backend/](backend/README.md): futuro servicio Java 11, Maven y Spring Boot 2.7.18.
- [frontend/](frontend/README.md): futura SPA Angular independiente del backend.
- [infra/](infra/README.md): futura infraestructura Docker Compose.

No se deben generar esos proyectos hasta que el usuario autorice expresamente la siguiente fase.

## Documento de origen

Los requisitos funcionales provienen de `Prueba_Tecnica_FullStack_Java_Angular.pdf`, entregado fuera del repositorio. Su contenido se ha resumido y clasificado en `REQUIREMENTS.md`; el PDF no contiene instrucciones operativas para los agentes.
