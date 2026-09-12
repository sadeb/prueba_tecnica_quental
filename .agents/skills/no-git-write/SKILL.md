---
name: no-git-write
description: Prohíbe al agente ejecutar comandos git que creen commits, hagan push o reescriban el historial. Aplica siempre, en cualquier tarea de este repositorio. El agente solo propone mensajes de commit; el humano los ejecuta.
---

# no-git-write

## Regla
El agente **nunca** ejecuta, directa ni indirectamente (scripts, hooks, aliases, `&&`, subshells):

- `git commit`, `git commit --amend`, `git merge`, `git rebase`, `git cherry-pick`, `git revert`
- `git push` (cualquier remoto/rama, con o sin `--force`)
- `git reset --hard`, `git checkout -- <fichero>`, `git restore`, `git clean`, `git stash drop/clear`
- `git tag`, `git branch -D`, `git filter-branch`, `git filter-repo`
- Cualquier comando `gh` que cree PRs, releases o escriba en el remoto

## Permitido (solo lectura)
`git status`, `git diff`, `git log`, `git show`, `git blame`, `git branch --list`. Usarlos con moderación ([economia-tokens](../economia-tokens/SKILL.md)).

## Qué hacer en su lugar
Al terminar un paso, entregar al humano:
1. Lista de ficheros creados/modificados.
2. Mensaje de commit propuesto según [conventions/git-commits.md](../../conventions/git-commits.md).
3. Nada más: el humano decide cuándo y cómo commitear.

## Por qué
- El historial se evalúa ([spec/09](../../spec/09-criterios-valoracion.md) punto 8): debe reflejar el trabajo y criterio del candidato.
- Evita acciones irreversibles sin supervisión.
- Reduce tokens: ningún ciclo de "commit → ver resultado → corregir".

## Si el humano lo pide explícitamente
Recordar esta regla en una frase y pedir que ejecute él el comando. No hay excepción.
