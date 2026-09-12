# Angular + Bootstrap

**Versión exacta**: anotar aquí tras `ng new` (CLI, Angular core, Node, runner de tests). Hasta entonces: última LTS del CLI.

## Proyecto
- `ng new frontend --standalone --routing --style=scss --skip-git`. Standalone components, sin NgModules.
- Bootstrap 5.3 por npm; importar `bootstrap/scss/bootstrap` en `styles.scss` (o el CSS en `angular.json > styles`). **Sin** `ng-bootstrap`/`ngx-bootstrap`: el JS de Bootstrap no se necesita para las pantallas mínimas (usar clases de utilidad y componentes CSS).
- `environment.ts`: `apiBaseUrl` (`http://localhost:8080/api` en dev; en compose, mismo host vía nginx o proxy).
- Proxy dev: `proxy.conf.json` → `/api` → backend, evita CORS en desarrollo.

## Piezas idiomáticas (API moderna, standalone)
- **HTTP**: `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))`.
- **Interceptor funcional** (`HttpInterceptorFn`): añade `Authorization: Bearer <token>`; en 401 → limpia sesión y redirige a `/login`; normaliza errores a un `ApiError` propio ([conventions/formato-error.md](../conventions/formato-error.md)).
- **Guarda funcional** (`CanActivateFn`): `authGuard` redirige a `/login` guardando `returnUrl`.
- **Estado**: servicios con `signal()`/`computed()` o `BehaviorSubject`. Sin NgRx (economía de dependencias).
- **Token**: `localStorage` (persistencia entre recargas); leer al arrancar en `AuthService`.
- **Formularios**: `ReactiveFormsModule` con validadores; mostrar errores con clases `is-invalid`/`invalid-feedback` de Bootstrap.
- **Estados de vista**: modelo `{ status: 'loading'|'empty'|'error'|'ready', data?, error? }` por vista; plantilla con `@if`/`@for` (control flow de Angular 17+).
- **Rutas**: `/login`, `/register`, `/characters`, `/characters/:id`, `/favorites`; lazy `loadComponent`.

## Docker
Build multi-stage `node:20-alpine` → `nginx:alpine` sirviendo `dist/` con `try_files $uri /index.html` y proxy `/api` al backend.

Relacionado: [conventions/angular.md](../conventions/angular.md), [spec/06-frontend-angular.md](../spec/06-frontend-angular.md), [testing-frontend.md](testing-frontend.md).
