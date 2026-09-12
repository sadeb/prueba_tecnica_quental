# 09 · Favoritos por usuario

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md)

## Objetivo
Añadir, listar y eliminar personajes favoritos del usuario autenticado.

## Contexto
[conventions/api-rest.md](../conventions/api-rest.md), [spec/05](../spec/05-api-propia.md) punto 3, [ADR-005](../decisions/ADR-005-autenticacion.md).

## Pasos
1. Migración `V4__user_favorites.sql`: `user_favorites(user_id FK, character_id FK, created_at, PK(user_id, character_id))`.
2. Entidad `Favorite` (o `@ManyToMany` en `User`; preferir entidad explícita por claridad) + repositorio.
3. `FavoriteService`: `add` (404 si el personaje no existe; repetido → decidir idempotente 200 o 409 y documentar en [api-rest](../conventions/api-rest.md)), `list` (ordenado por `created_at`), `remove` (404 si no era favorito).
4. `FavoriteController` bajo `/api/users/me/favorites`, usuario desde `SecurityContext`.
5. Tests `@WebMvcTest` (401 sin token, 201, 404, repetido) y de servicio.

## Hecho cuando
Flujo completo con `curl` usando el token del [08](08-autenticacion.md).

## Ejecuta y pega
```bash
cd backend && ./mvnw -q test -Dtest='Favorite*Test' && echo TESTS_OK
```

## Commit propuesto
`feat(api): manage per-user favorite characters`
