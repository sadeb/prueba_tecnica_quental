# Convenciones frontend (Angular standalone · Bootstrap)

## Estructura
```
src/app
├── core/                # singletons: auth.service, auth.guard, auth.interceptor, error.interceptor, api-error model, token.storage
├── shared/              # presentacionales reutilizables: loading, empty-state, error-alert, pagination, character-card
├── features/
│   ├── auth/            # login.page, register.page (+ formularios)
│   ├── characters/      # character-list.page, character-detail.page, character.service, character.model, filters
│   └── favorites/       # favorites.page, favorite.service
├── app.routes.ts
└── app.config.ts        # provideHttpClient(withInterceptors([...])), provideRouter
```
- **Página (contenedor)** orquesta servicios y estado; sufijo `.page.ts` o `-page.component.ts`. **Presentacional** solo recibe `input()` y emite `output()`; va en `shared/` si es reutilizable.
- Un servicio por recurso de la API en `core/` o `features/*/`; es el único lugar con `HttpClient`.
- Estado por vista con `ViewState<T>` (`loading | empty | error | ready`) en `shared/`; la plantilla cubre los 4 estados siempre ([spec/06](../spec/06-frontend-angular.md) punto 5).
- Filtros y página en **query params** de la ruta (`?name=&status=&page=`): recargar conserva el estado y es testeable.
- Errores: el interceptor convierte `HttpErrorResponse` en `ApiError` ([formato-error.md](formato-error.md)); las páginas muestran `error.message`.
- Bootstrap: clases utilitarias y componentes CSS (`card`, `form-control`, `btn`, `alert`, `spinner-border`, `pagination`). Sin CSS custom salvo lo imprescindible.
- Reglas de escritura (TypeScript, componentes, plantillas, accesibilidad, signals, formularios, servicios, reglas por versión, lista de prohibido): skill [angular-spa](../skills/angular-spa/SKILL.md) y su hoja por tipo de fichero ([ADR-007](../decisions/ADR-007-best-practices-angular.md)). Este fichero no las repite.

Relacionado: [nomenclatura.md](nomenclatura.md), [references/angular-bootstrap.md](../references/angular-bootstrap.md), [references/testing-frontend.md](../references/testing-frontend.md).
