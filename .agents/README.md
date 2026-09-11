# Contexto compartido para agentes

Este directorio reúne información neutral respecto al proveedor del agente. `AGENTS.md` contiene las reglas operativas canónicas; aquí se conserva el detalle del producto y la continuidad entre sesiones.

## Navegación

- `context/REQUIREMENTS.md`: requisitos y trazabilidad.
- `context/ARCHITECTURE.md`: arquitectura objetivo y flujos.
- `context/STACK.md`: tecnologías y compatibilidad prevista.
- `context/API_CONTRACT.md`: superficie HTTP futura de alto nivel.
- `context/ROADMAP.md`: fases, entregables y puertas de aprobación.
- `context/HANDOFF.md`: fotografía breve del estado actual.
- `context/SKILLS_CANDIDATES.md`: skills que el usuario podrá evaluar manualmente.
- `decisions/`: ADR aceptados, aunque todavía no implementados.
- `templates/`: formatos para nuevos ADR y futuros handoffs.

## Mantenimiento

- Evitar duplicar reglas de `AGENTS.md`.
- Marcar siempre si algo está `PLANIFICADO`, `EN CURSO` o `IMPLEMENTADO`.
- Distinguir requisitos del PDF, decisiones del usuario e inferencias de diseño.
- No crear `.agents/skills/` ni archivos `SKILL.md` durante `CONTEXT_BOOTSTRAP`.
