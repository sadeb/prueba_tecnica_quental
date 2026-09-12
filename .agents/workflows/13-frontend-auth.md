# 13 · Frontend: registro, login y sesión

**Rol**: [desarrollador-frontend](../agents/desarrollador-frontend.md) · Skill: [angular-spa](../skills/angular-spa/SKILL.md)

## Objetivo
Páginas de registro e inicio de sesión, persistencia del token, guarda de rutas y manejo central de 401.

## Contexto
[spec/06](../spec/06-frontend-angular.md) puntos 2–4, [conventions/api-rest.md](../conventions/api-rest.md) (`/api/auth/*`), [conventions/angular.md](../conventions/angular.md).

## Pasos
1. `AuthService`: `login()`, `register()`, `logout()`, `isAuthenticated` (signal derivado del token), `username`; lee `TokenStorage` al construirse.
2. `TokenStorage`: `get/set/clear` sobre `localStorage`, con try/catch.
3. `authInterceptor`: añade `Authorization` si hay token y la URL es de `apiBaseUrl`.
4. `errorInterceptor`: mapea a `ApiError`; 401 → `logout()` + navegación a `/login` con `returnUrl`; deja 4xx/5xx como `ApiError` para las páginas.
5. `authGuard`: protege `/favorites` (y las que se decidan).
6. `login.page` y `register.page`: formularios reactivos, estados `submitting`/`error`, feedback Bootstrap, redirección a `returnUrl` o `/characters`.
7. Navbar reacciona a `isAuthenticated`.

## Hecho cuando
Recargar mantiene la sesión; token manipulado en `localStorage` → primera llamada protegida devuelve 401 → vuelta a `/login` sin pantalla rota.

## Ejecuta y pega
Comprobación manual en el navegador; pegar solo errores de consola si los hay.

## Commit propuesto
`feat(frontend): add login, registration, token persistence, auth guard and 401 handling`
