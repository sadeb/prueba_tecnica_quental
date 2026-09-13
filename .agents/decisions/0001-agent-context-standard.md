# ADR 0001: `AGENTS.md` como contexto canónico

- Estado: Aceptado
- Fecha: 2026-09-11
- Alcance de implementación: Fase 0

## Contexto

El proyecto será desarrollado alternando distintos agentes de IA por razones de coste y disponibilidad. Mantener instrucciones completas para cada proveedor causaría divergencias y consumo innecesario de contexto.

## Decisión

- Usar `AGENTS.md` en la raíz como fuente canónica de instrucciones operativas.
- Guardar requisitos, arquitectura, roadmap, decisiones y handoff en `.agents/` con Markdown neutral.
- Usar `CLAUDE.md` únicamente para importar `@AGENTS.md`.
- Configurar Gemini para reconocer `AGENTS.md` mediante `.gemini/settings.json`.
- Permitir a Codex, OpenCode y agentes compatibles leer `AGENTS.md` directamente.
- No duplicar reglas completas en archivos específicos de proveedores.

## Consecuencias

- Todos los agentes comparten una misma fuente de verdad y un handoff compacto.
- Un cambio de regla requiere editar un solo documento canónico.
- Agentes sin soporte de `AGENTS.md` necesitarán un adaptador mínimo explícito.
- Las instrucciones del usuario siguen teniendo prioridad sobre cualquier archivo del repositorio.
