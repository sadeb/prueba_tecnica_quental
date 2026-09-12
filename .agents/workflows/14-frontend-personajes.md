# 14 · Frontend: listado y detalle de personajes

**Rol**: [desarrollador-frontend](../agents/desarrollador-frontend.md) · Skill: [angular-spa](../skills/angular-spa/SKILL.md)

## Objetivo
Listado con filtros (name, status, species, gender) y paginación; detalle con episodios, localizaciones y personajes relacionados; los cuatro estados de vista en ambas.

## Contexto
[spec/06](../spec/06-frontend-angular.md) puntos 3 y 5, [conventions/api-rest.md](../conventions/api-rest.md) (`/api/characters*`), [conventions/angular.md](../conventions/angular.md).

## Pasos
1. `character.model.ts` (`CharacterSummary`, `CharacterDetail`, `RelatedCharacter`, `PageResponse<T>`, `CharacterFilters`).
2. `CharacterService`: `search(filters, page, size)`, `getById(id)`, `getRelated(id, limit)`; params solo si tienen valor.
3. `character-filters` (presentacional): formulario con `output` `filtersChange` (debounce en `name`).
4. `character-card` (presentacional) y `character-list.page`: filtros y página desde query params; cambiar filtro → `page=0`; `ViewState` con `loading/empty/error/ready`; `pagination` compartida.
5. `character-detail.page`: carga detalle y relacionados en paralelo (`forkJoin`) o con estados independientes (preferible: el fallo de relacionados no rompe el detalle); enlaces a otros personajes; botón favorito si autenticado (se conecta en [15](15-frontend-favoritos.md)).
6. Imágenes con `loading="lazy"` y `alt`.

## Hecho cuando
Filtrar, paginar, recargar (estado conservado) y ver detalle con relacionados; apagar el backend muestra la alerta de error con "Reintentar".

## Ejecuta y pega
Comprobación manual; pegar errores de consola si los hay.

## Commit propuesto
`feat(frontend): add character list with filters and pagination and detail with related characters`
