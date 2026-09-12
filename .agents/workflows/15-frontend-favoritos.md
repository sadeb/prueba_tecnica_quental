# 15 · Frontend: gestión de favoritos

**Rol**: [desarrollador-frontend](../agents/desarrollador-frontend.md) · Skill: [angular-spa](../skills/angular-spa/SKILL.md)

## Objetivo
Página protegida de favoritos (listar y eliminar) y acción añadir/quitar desde tarjeta y detalle.

## Contexto
[conventions/api-rest.md](../conventions/api-rest.md) (`/api/users/me/favorites*`), [spec/06](../spec/06-frontend-angular.md) punto 3, [conventions/angular.md](../conventions/angular.md).

## Pasos
1. `FavoriteService`: `list()`, `add(characterId)`, `remove(characterId)`; mantiene un `signal<Set<number>>` de ids favoritos cargado al autenticarse y limpiado en `logout`.
2. `favorite-toggle` (presentacional): `input` `isFavorite`, `input` `disabled`, `output` `toggle`.
3. `favorites.page` (ruta protegida): `ViewState`, estado vacío con enlace a `/characters`, eliminar con actualización optimista y reversión en error.
4. Integrar toggle en `character-card` y `character-detail.page`; sin sesión, el toggle lleva a `/login` con `returnUrl`.
5. Errores 404/409 del backend mostrados con `error-alert` sin romper la lista.

## Hecho cuando
Añadir desde lista, ver en favoritos, eliminar, recargar: coherente con el backend.

## Ejecuta y pega
Comprobación manual; pegar errores de consola si los hay.

## Commit propuesto
`feat(frontend): manage favorite characters for the authenticated user`
