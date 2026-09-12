# agents/ — Roles de agente

Cada fichero define un rol: qué hace, qué **no** hace, qué contexto carga y qué entrega. Un agente adopta **un** rol por tarea. Todos heredan las reglas de [AGENTS.md](../../AGENTS.md) y las skills obligatorias ([no-git-write](../skills/no-git-write/SKILL.md), [no-run-commands](../skills/no-run-commands/SKILL.md), [economia-tokens](../skills/economia-tokens/SKILL.md)).

| Rol | Cuándo usarlo |
|---|---|
| [orquestador.md](orquestador.md) | Planificar, elegir workflow, repartir trabajo, revisar coherencia global |
| [arquitecto-backend.md](arquitecto-backend.md) | Decidir estructura, modelo de datos, contratos; redactar ADRs |
| [desarrollador-backend.md](desarrollador-backend.md) | Implementar Java/Spring: cliente externo, sync, persistencia, API |
| [desarrollador-frontend.md](desarrollador-frontend.md) | Implementar Angular: servicios, guardas, interceptores, páginas |
| [ingeniero-infra.md](ingeniero-infra.md) | docker-compose, Dockerfiles, configuración de entorno |
| [qa-tester.md](qa-tester.md) | Diseñar y escribir pruebas backend y frontend, casos límite |
| [revisor-codigo.md](revisor-codigo.md) | Revisar contra spec, convenciones y criterios de valoración |
| [redactor-docs.md](redactor-docs.md) | README de entrega, OpenAPI, notas para la entrevista |

Formato de cada rol: Misión · Carga de contexto · Entrega · Límites · Checklist de salida.
