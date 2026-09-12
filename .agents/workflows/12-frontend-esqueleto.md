# 12 · Esqueleto frontend

**Rol**: [desarrollador-frontend](../agents/desarrollador-frontend.md) · Skills: [angular-spa](../skills/angular-spa/SKILL.md), [angular-best-practices](../skills/angular-best-practices/SKILL.md)

## Objetivo
Proyecto Angular standalone con Bootstrap, estructura de carpetas, `HttpClient` con interceptores registrados, rutas base, componentes compartidos de estado y Dockerfile con nginx.

## Contexto
[conventions/angular.md](../conventions/angular.md), [references/angular-bootstrap.md](../references/angular-bootstrap.md), [conventions/formato-error.md](../conventions/formato-error.md).

## Pasos
1. El **humano** ejecuta `ng new frontend --standalone --routing --style=scss --skip-git` y `npm i bootstrap@5.3` (el agente no ejecuta: [no-run-commands](../skills/no-run-commands/SKILL.md)). Anotar versiones en [angular-bootstrap.md](../references/angular-bootstrap.md) y marcar qué reglas por versión aplican ([angular-best-practices](../skills/angular-best-practices/SKILL.md#reglas-que-dependen-de-la-versión)).
2. `styles.scss` importa Bootstrap. `strict: true`.
3. `environments/`: `apiBaseUrl`. `proxy.conf.json` para `/api` → `http://localhost:8080`.
4. `core/`: `api-error.model.ts`, `token.storage.ts`, `auth.service.ts` (esqueleto), `auth.interceptor.ts`, `error.interceptor.ts`, `auth.guard.ts` (se completan en [13](13-frontend-auth.md)).
5. `shared/`: `loading-spinner`, `empty-state`, `error-alert` (con output `retry`), `pagination`; modelo `ViewState<T>`.
6. `app.config.ts` con `provideRouter`, `provideHttpClient(withInterceptors([...]))`. `app.routes.ts` con rutas lazy y redirección raíz → `/characters`.
7. Layout: navbar Bootstrap con enlaces `Characters`, `Favorites`, `Login/Logout`.
8. `frontend/Dockerfile` (node build → nginx) y `nginx.conf` (SPA fallback + proxy `/api`). Descomentar `frontend` en compose.

## Hecho cuando
`ng build` sin errores; la app arranca y muestra el layout con rutas vacías.

## Ejecuta y pega
```bash
cd frontend && npx ng build --configuration development 2>&1 | tail -5
```

## Commit propuesto
`chore(frontend): scaffold angular standalone app with bootstrap, interceptors and shared state components`
