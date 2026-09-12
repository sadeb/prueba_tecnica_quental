# Fuente externa · Rick and Morty API

Base: `https://rickandmortyapi.com/api`. REST, JSON, sin autenticación ni clave. Sin límite de tasa documentado: ser respetuoso (secuencial, sin paralelizar).

## Endpoints usados
| Recurso | Listado paginado | Detalle | Total aprox. |
|---|---|---|---|
| Character | `GET /character?page=N` | `GET /character/{id}` | 826 (42 páginas) |
| Episode | `GET /episode?page=N` | `GET /episode/{id}` | 51 (3 páginas) |
| Location | `GET /location?page=N` | `GET /location/{id}` | 126 (7 páginas) |

Paginación: 20 elementos/página. Respuesta: `{ "info": { "count", "pages", "next", "prev" }, "results": [ ... ] }`. `next`/`prev` son URLs completas o `null`.

## Campos por recurso (los que importan)
- **Character**: `id`, `name`, `status` (`Alive|Dead|unknown`), `species`, `type` (puede ser `""`), `gender` (`Female|Male|Genderless|unknown`), `origin {name, url}`, `location {name, url}`, `image` (URL), `episode` (array de URLs), `url`, `created`.
- **Episode**: `id`, `name`, `air_date` (texto libre, p. ej. `"December 2, 2013"`), `episode` (código `S01E01`), `characters` (array de URLs), `url`, `created`.
- **Location**: `id`, `name`, `type`, `dimension`, `residents` (array de URLs), `url`, `created`.

## Inconsistencias conocidas (tratar explícitamente)
- `origin`/`location` pueden ser `{ "name": "unknown", "url": "" }` → **no hay id**; el personaje queda sin esa relación (nullable).
- `type`, `dimension` pueden ser `""` → normalizar a `null` o conservar `""` (decidir en [ADR-001](../decisions/ADR-001-identificador-externo.md)).
- `air_date` no es ISO → guardar como texto; no parsear a fecha.
- Las relaciones vienen como **URLs** (`.../episode/28`): extraer el id numérico del último segmento; validar que sea entero.
- 404 devuelve `{ "error": "There is nothing here" }` con estado 404.
- Página fuera de rango → 404.
- Un personaje puede tener 0 episodios en teoría; no asumir mínimo.

Relacionado: [spec/02-integracion-api-externa.md](../spec/02-integracion-api-externa.md), [workflows/03-cliente-api-externa.md](../workflows/03-cliente-api-externa.md).
