# Angular + Bootstrap

**Versión exacta**: anotar aquí tras `ng new` (CLI, Angular core, Node, runner de tests) y marcar qué filas de la tabla por versión de [skills/angular-spa](../skills/angular-spa/SKILL.md#reglas-que-dependen-de-la-versión) aplican (`standalone` implícito, `OnPush` implícito, Signal Forms, `@Service`). Hasta entonces: última LTS del CLI.

## Proyecto
- `ng new frontend --standalone --routing --style=scss --skip-git`. Standalone components, sin NgModules.
- Bootstrap 5.3 por npm; importar `bootstrap/scss/bootstrap` en `styles.scss` (o el CSS en `angular.json > styles`). **Sin** `ng-bootstrap`/`ngx-bootstrap`: el JS de Bootstrap no se necesita para las pantallas mínimas (usar clases de utilidad y componentes CSS).
- `environment.ts`: `apiBaseUrl` (`http://localhost:8080/api` en dev; en compose, mismo host vía nginx o proxy).
- Proxy dev: `proxy.conf.json` → `/api` → backend, evita CORS en desarrollo.

- Rutas: `/login`, `/register`, `/characters`, `/characters/:id`, `/favorites`; raíz → `/characters`; lazy `loadComponent`.
- Token de sesión en `localStorage` (persistencia entre recargas), solo a través de `TokenStorage`.

Cómo se escriben servicios, interceptores, guardas, estado, formularios y plantillas: hojas de [skills/angular-spa](../skills/angular-spa/SKILL.md); este fichero no lo repite. Documentación oficial filtrada: [angular-oficial/](angular-oficial/README.md).

## Docker
Build multi-stage `node:20-alpine` → `nginx:alpine` sirviendo `dist/` con `try_files $uri /index.html` y proxy `/api` al backend.

Relacionado: [conventions/angular.md](../conventions/angular.md), [spec/06-frontend-angular.md](../spec/06-frontend-angular.md), [testing-frontend.md](testing-frontend.md).
