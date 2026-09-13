# ADR-005 · Autenticación con token propio

**Estado**: Propuesta (ampliada por [ADR-012](ADR-012-administrador-sistema.md): rol `ADMIN` y administrador desde `.env`) · **Requisito**: [spec/05](../spec/05-api-propia.md) puntos 2–3; bonus B1 ([spec/08](../spec/08-bonus.md)) · **Fecha**: 2026-09-12

## Contexto
Se piden registro, login y favoritos por usuario autenticado. Bonus: tokens propios sin librerías de terceros para emisión/validación. No se pide seguridad avanzada. Spring Security forma parte del framework (economía de dependencias).

## Decisión
- **Token propio con formato JWT compacto firmado con HMAC-SHA256** usando `javax.crypto.Mac` y `Base64.getUrlEncoder()`: `header.payload.signature`. Claims: `sub` (username), `uid`, `iat`, `exp` (por defecto 8 h). Clave secreta desde propiedad `auth.token.secret` (en `.env`). Sin `jjwt`/`nimbus`.
- Validación: firma con comparación en tiempo constante (`MessageDigest.isEqual`), `exp` no vencido, estructura de 3 partes. Cualquier fallo → 401 `UNAUTHORIZED` ([formato-error](../conventions/formato-error.md)).
- **Spring Security** solo para: `BCryptPasswordEncoder`, un `OncePerRequestFilter` que lee `Authorization: Bearer` y puebla el `SecurityContext`, y la configuración de rutas públicas/protegidas. Sesión `STATELESS`, CSRF deshabilitado (API sin cookies), CORS permitido para el origen del frontend.
- Rutas protegidas: `/api/users/me/**`, `/api/admin/**`. Públicas: `/api/auth/**`, listados/detalle de datos sincronizados, OpenAPI, `/actuator/health`.
- `/api/admin/sync`: ~~protegido con token de cualquier usuario autenticado (no hay roles en el alcance)~~ → desde [ADR-012](ADR-012-administrador-sistema.md) exige rol `ADMIN` (claim `role` en el token; 403 si falta).
- Registro: `username` único (409 si existe), `password` mínimo 8 caracteres, almacenada con BCrypt.
- Sin refresh token ni revocación: fuera de alcance; se dice explícitamente en README. Roles mínimos (`USER`/`ADMIN`) según [ADR-012](ADR-012-administrador-sistema.md).

## Alternativas descartadas
- `spring-security-oauth2-resource-server` + `jjwt`: válido pero anula el bonus B1 y añade dependencias.
- Token opaco en tabla `sessions`: más simple aún, pero exige consulta a BD por petición y no muestra dominio de firma/expiración.
- Sesión con cookie: no encaja con SPA + guardas + persistencia de token pedidas en [spec/06](../spec/06-frontend-angular.md).

## Consecuencias
Cero dependencias extra; código de token pequeño y testeable (firma, expiración, manipulación). Riesgo: implementar mal la comparación o el Base64; cubrir con tests unitarios.

## Dónde se aplica
[wf 08](../workflows/08-autenticacion.md), [wf 09](../workflows/09-favoritos.md), [wf 13](../workflows/13-frontend-auth.md).
