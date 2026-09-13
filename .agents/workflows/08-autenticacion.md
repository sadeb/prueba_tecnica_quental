# 08 · Autenticación (registro, login, token propio)

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [spring-boot-jdk11](../skills/spring-boot-jdk11/SKILL.md)

## Objetivo
Registro y login con token propio HMAC-SHA256, filtro de autenticación y rutas protegidas.

## Contexto
[ADR-005](../decisions/ADR-005-autenticacion.md), [conventions/api-rest.md](../conventions/api-rest.md), [conventions/formato-error.md](../conventions/formato-error.md), [spec/05](../spec/05-api-propia.md).

## Pasos
1. Changesets `003-users.sql`: `users(id, username UNIQUE, password_hash, created_at)` y `005-users-role.sql`: columna `role` (`USER`/`ADMIN`, [ADR-012](../decisions/ADR-012-administrador-sistema.md)).
2. `User` entidad + `UserRepository`.
3. `TokenService`: `issue(user)` y `parse(token)` con `javax.crypto.Mac`, Base64 URL sin padding, claims `sub`, `uid`, `role`, `iat`, `exp`; secreto y TTL desde `auth.token.*`. Errores → `InvalidTokenException`.
4. `AuthService`: `register` (409 si existe, BCrypt), `login` (401 si no coincide).
5. `BearerTokenFilter` (`OncePerRequestFilter`): lee cabecera, valida, puebla `SecurityContext` con `UsernamePasswordAuthenticationToken`; token inválido → responde 401 con `ApiError` directamente (el filtro está fuera del `ControllerAdvice`).
6. `SecurityConfig` definitiva: stateless, CSRF off, CORS para el origen del frontend, públicas/protegidas según ADR-005 (`/api/admin/**` con `hasRole("ADMIN")`), `AuthenticationEntryPoint` que devuelve `ApiError` 401 y `AccessDeniedHandler` que devuelve `ApiError` 403.
6b. `AdminUserProperties` (`auth.admin.*`, desde `ADMIN_USERNAME`/`ADMIN_PASSWORD` en `.env`) + `AdminUserInitializer` (`ApplicationRunner`): crea o actualiza el administrador con rol `ADMIN` al arrancar (ADR-012).
7. `AuthController`: `POST /api/auth/register` (201), `POST /api/auth/login` (200 `{token, expiresAt, username}`).
8. Tests: `TokenServiceTest` (firma alterada, caducado, formato inválido, roundtrip), `AuthControllerTest` (`@WebMvcTest`: 201/409/400/401), filtro con y sin token.

## Hecho cuando
Login devuelve token; `GET /api/users/me/favorites` sin token → 401 con `ApiError`; con token → 200 (aunque vacío hasta [09](09-favoritos.md)).

## Ejecuta y pega
```bash
cd projects/backend && ./mvnw -q test -Dtest='TokenServiceTest,AuthControllerTest,BearerTokenFilterTest' && echo TESTS_OK
```

## Commit propuesto
`feat(auth): add user registration, login and self-issued hmac tokens with stateless security filter`
