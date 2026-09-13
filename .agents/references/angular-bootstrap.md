# Angular + Bootstrap

**Versión exacta**: anotar aquí tras `ng new` (CLI, Angular core, Node, runner de tests) y marcar qué filas de la tabla por versión de [skills/angular-spa](../skills/angular-spa/SKILL.md#reglas-que-dependen-de-la-versión) aplican (`standalone` implícito, `OnPush` implícito, Signal Forms, `@Service`). Hasta entonces: última LTS del CLI.

## Proyecto
- `ng new frontend --standalone --routing --style=scss --skip-git`. Standalone components, sin NgModules.
- Bootstrap 5.3 por npm; importar `bootstrap/scss/bootstrap` en `styles.scss` (o el CSS en `angular.json > styles`). **Sin** `ng-bootstrap`/`ngx-bootstrap`: el JS de Bootstrap no se necesita para las pantallas mínimas (usar clases de utilidad y componentes CSS).
- `environment.ts`: `apiBaseUrl = '/api'` relativo en **todos** los environments: en compose lo sirve nginx (proxy a `backend:8080`) y en `ng serve` lo resuelve `proxy.conf.json`. Así no hace falta CORS en el backend en ningún escenario.
- Proxy dev: `proxy.conf.json` → `/api` → backend, evita CORS en desarrollo.

- Rutas: `/login`, `/register`, `/characters`, `/characters/:id`, `/favorites`; raíz → `/characters`; lazy `loadComponent`.
- Token de sesión en `localStorage` (persistencia entre recargas), solo a través de `TokenStorage`.

Cómo se escriben servicios, interceptores, guardas, estado, formularios y plantillas: hojas de [skills/angular-spa](../skills/angular-spa/SKILL.md); este fichero no lo repite. Documentación oficial filtrada: [angular-oficial/](angular-oficial/README.md).

## Docker
`projects/frontend/Dockerfile` (ya existe): build multi-stage `node:24.21.0-alpine` (`npm ci` con cache de BuildKit, `npm run build`) → `nginx:1.30.4-alpine-slim` con `nginx.conf` completo (1 worker, `try_files $uri /index.html`, `location ^~ /api/` con proxy al backend vía `resolver 127.0.0.11`, bundles con hash inmutables e `index.html` sin cache). Copia `dist/frontend/browser/`: el nombre del proyecto debe ser `frontend` y sin SSR. `.dockerignore` excluye `node_modules`, `dist`, `.angular`, `**/*.spec.ts`, `proxy.conf.json`.

Relacionado: [conventions/angular.md](../conventions/angular.md), [spec/06-frontend-angular.md](../spec/06-frontend-angular.md), [testing-frontend.md](testing-frontend.md).
