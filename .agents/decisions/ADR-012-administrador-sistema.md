# ADR-012 · Administrador del sistema configurado en `.env` y rol ADMIN

**Estado**: Propuesta · **Requisito**: [spec/03](../spec/03-sincronizacion-kafka.md) punto 1 (endpoint de administración); [spec/05](../spec/05-api-propia.md); amplía [ADR-005](ADR-005-autenticacion.md) · **Fecha**: 2026-09-13

## Contexto
El usuario y la contraseña del administrador del sistema deben configurarse en `projects/.env`. Hasta ahora no había roles: cualquier usuario registrado podía lanzar `POST /api/admin/sync` (limitación anotada en README y ADR-005). Un administrador configurado sin nada que solo él pueda hacer no tendría sentido.

## Decisión
- Variables `ADMIN_USERNAME` / `ADMIN_PASSWORD` en `.env`; el compose las pasa al backend como `AUTH_ADMIN_USERNAME` / `AUTH_ADMIN_PASSWORD` (relaxed binding a `auth.admin.*`, `AdminUserProperties`, validadas con las mismas reglas que el registro: usuario 3–64 `[A-Za-z0-9._-]`, contraseña 8–72). `application.yml` trae valores de desarrollo para ejecutar fuera del compose.
- Columna `users.role` (`USER` | `ADMIN`, default `USER`) añadida por el changeset `005-users-role` ([ADR-011](ADR-011-liquibase-migraciones.md)). El registro público siempre crea `USER`; no existe endpoint para cambiar roles.
- `AdminUserInitializer` (`ApplicationRunner`, `@Order(0)`, tras Liquibase) crea el administrador si no existe y, si existe, garantiza rol `ADMIN` y rehace el hash BCrypt solo cuando la contraseña configurada ya no coincide. Idempotente: sin cambios, no escribe. Cambiar la contraseña en `.env` se aplica en el siguiente arranque sin tocar volúmenes.
- El token propio lleva el claim `role`; es obligatorio (un token sin él o con valor desconocido → 401). `BearerTokenFilter` concede `ROLE_<role>`. `/api/admin/**` exige `hasRole("ADMIN")`; `/api/users/me/**` sigue con cualquier usuario autenticado.
- Token válido sin rol suficiente → **403 `FORBIDDEN`** con `ApiError` (`ApiErrorAccessDeniedHandler`, porque la decisión ocurre en la cadena de filtros y no llega al `ControllerAdvice`). Sin token sigue siendo 401.

## Alternativas descartadas
- Sembrar el administrador desde un changeset de Liquibase — BCrypt lleva sal aleatoria y la contraseña vive en `.env`, no en un SQL versionado; además un changeset es inmutable y la contraseña no.
- Usuario admin "hardcodeado" en `SecurityConfig` (in-memory) — no tendría fila en `users` ni podría tener favoritos; dos mecanismos de autenticación para lo mismo.
- Mantener sin roles y solo crear la cuenta — el requisito quedaría cumplido en la forma pero el administrador no sería distinto de cualquier otro usuario.
- Rol dentro de `AuthenticatedUser` con `@PreAuthorize` — método seguro pero más piezas (`@EnableGlobalMethodSecurity`); las rutas de admin comparten prefijo y basta el matcher.

## Consecuencias
Un solo lugar (`.env`) para las credenciales de administración; el endpoint de sincronización deja de estar abierto a cualquier usuario. Los tokens emitidos antes de este cambio dejan de ser válidos (falta `role`): basta volver a iniciar sesión. Cambiar `ADMIN_USERNAME` crea otro administrador y **no** degrada al anterior (se documenta; degradarlo exigiría una política de "único admin" que no se pide). En entrevista: "el administrador es un usuario normal con rol, provisionado por configuración, y el rol viaja firmado en el token".

## Dónde se aplica
`auth/AdminUserProperties`, `auth/AdminUserInitializer`, `auth/TokenService`, `auth/BearerTokenFilter`, `auth/ApiErrorAccessDeniedHandler`, `config/SecurityConfig`, `user/UserRole`, changeset `005-users-role`; `projects/.env`, `projects/docker-compose.yml`; [wf 08](../workflows/08-autenticacion.md).
