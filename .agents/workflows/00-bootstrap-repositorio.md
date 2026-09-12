# 00 · Bootstrap del repositorio

**Rol**: [ingeniero-infra](../agents/ingeniero-infra.md) / [orquestador](../agents/orquestador.md)

## Objetivo
Dejar la raíz del monorepo preparada: carpetas, `.gitignore`, `.env`, README mínimo con estructura.

## Contexto
[conventions/estructura-repositorio.md](../conventions/estructura-repositorio.md), [references/stack-versiones.md](../references/stack-versiones.md).

## Pasos
1. Crear `backend/` y `frontend/` vacíos (los generadores se aplican en [02](02-backend-esqueleto.md) y [12](12-frontend-esqueleto.md)).
2. Ampliar `.gitignore` raíz: `target/`, `node_modules/`, `dist/`, `.angular/`, `*.log`, `.idea/`, `.vscode/`, `.DS_Store`, `*.iml`. Mantener las reglas existentes.
3. Crear `.env` con credenciales de desarrollo: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `NEO4J_PASSWORD`, `AUTH_TOKEN_SECRET`, `AUTH_TOKEN_TTL_HOURS`. Comentar que son valores de desarrollo.
4. Sustituir el README de una línea por un esqueleto con las secciones de [18](18-readme-entrega.md) vacías o con "pendiente".
5. Añadir `.nvmrc` (Node LTS) en la raíz o en `frontend/`.

## Hecho cuando
Estructura de [estructura-repositorio.md](../conventions/estructura-repositorio.md) existe (sin código todavía); `.env` no contiene secretos reales.

## Ejecuta y pega
Nada que ejecutar.

## Commit propuesto
`chore: bootstrap monorepo layout, gitignore and dev env`
