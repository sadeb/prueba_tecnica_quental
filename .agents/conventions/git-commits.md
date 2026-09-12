# Commits (los ejecuta el humano; el agente solo propone)

Criterio evaluado: "coherencia del historial de trabajo" ([spec/09-criterios-valoracion.md](../spec/09-criterios-valoracion.md)). El agente **nunca** ejecuta `git commit`/`push` ([skill no-git-write](../skills/no-git-write/SKILL.md)); al terminar un paso de workflow, **propone** el mensaje de commit.

## Formato: Conventional Commits, en inglés
`<tipo>(<ámbito>): <resumen imperativo, ≤ 72 chars>`

Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`, `build`, `ci`.
Ámbitos: `backend`, `frontend`, `infra`, `sync`, `api`, `auth`, `graph`, `db`, `docs`, `agents`.

Ejemplos:
- `chore(infra): add docker-compose with postgres 10, neo4j 4.4 and kafka 2.0.1`
- `feat(sync): publish characters, episodes and locations to kafka topics`
- `feat(graph): resolve related characters by shared episodes in neo4j`
- `test(sync): reprocessing the same message does not duplicate rows`

## Granularidad
- Un commit por paso de workflow o por unidad funcional coherente que compile.
- Tests junto al código que prueban, o en commit `test(...)` inmediato.
- Nunca mezclar backend y frontend en el mismo commit salvo contrato compartido.
- El orden de commits debe reflejar las [prioridades](../spec/prioridades.md): infra → sync → modelo → API → frontend → bonus.

Cuerpo opcional: una o dos líneas con el porqué, no el qué. Enlazar el ADR si aplica (`Refs: ADR-003`).
