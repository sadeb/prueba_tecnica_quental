# .agents/ — Índice del contexto para agentes de IA

Entrada principal: [`../AGENTS.md`](../AGENTS.md). Cada carpeta responde a una pregunta distinta. Carga solo lo que necesites.

| Carpeta | Pregunta que responde | Índice |
|---|---|---|
| `spec/` | ¿Qué hay que construir? (requisitos del PDF, segmentados) | [spec/README.md](spec/README.md) |
| `references/` | ¿Cómo funciona cada tecnología en las versiones fijadas? | [references/README.md](references/README.md) |
| `conventions/` | ¿Cómo se escribe y organiza el código y el repositorio? | [conventions/README.md](conventions/README.md) |
| `decisions/` | ¿Qué decisiones de diseño se han tomado y por qué? (ADRs) | [decisions/README.md](decisions/README.md) |
| `agents/` | ¿Qué rol asume el agente y con qué límites? | [agents/README.md](agents/README.md) |
| `skills/` | ¿Qué procedimientos y restricciones aplica el agente? | [skills/README.md](skills/README.md) |
| `workflows/` | ¿En qué orden y con qué pasos se construye cada pieza? | [workflows/README.md](workflows/README.md) |
| `glossary.md` | ¿Qué significa cada término del dominio? | [glossary.md](glossary.md) |

## Principios de estos ficheros
- Un fichero = un concepto o tarea. Minimalista. Sin repetir contenido: se enlaza.
- Los enlaces son relativos y siempre a ficheros existentes.
- Si un fichero supera ~80 líneas, se divide.
- Cuando cambie el código, se actualiza el fichero afectado (spec no cambia; decisions y references sí).
