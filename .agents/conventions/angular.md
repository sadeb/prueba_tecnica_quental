# Convenciones frontend (Angular standalone · Bootstrap)

## Estructura
```
src/app
├── core/
│   ├── auth/            # auth.service, auth.guard (auth/guest), auth.interceptor, token.storage, session.model
│   ├── errors/          # api-error.model, error-messages (es), error.interceptor, retry.interceptor, global-error.handler
│   ├── storage/         # browser-storage (único acceso a localStorage)
│   ├── theme/           # theme.service (data-bs-theme)
│   ├── notifications/   # toast.service
│   ├── layout/          # shell.page (layout autenticado), navbar, theme-toggle
│   └── routing/         # app-title.strategy
├── shared/
│   ├── models/          # view-state, page-response, character.model (lo usa character-card)
│   ├── i18n/            # labels (enumerados en español)
│   └── ui/              # loading, empty-state, error-alert, pagination, character-card, favorite-toggle, status-badge, toast-container, skeleton
├── features/
│   ├── auth/            # login.page (login + registro en pestañas; Signal Forms)
│   ├── characters/      # character-list.page, character-detail.page, character.service, character-filters
│   ├── favorites/       # favorites.page, favorite.service
│   └── not-found/       # not-found.page
├── app.routes.ts
└── app.config.ts        # provideHttpClient(withInterceptors([auth, retry, error])), provideRouter, ErrorHandler, TitleStrategy
```
Estructura real tras [ADR-010](../decisions/ADR-010-frontend-sesion-tema-errores.md).
- **Página (contenedor)** orquesta servicios y estado; sufijo `.page.ts` o `-page.component.ts`. **Presentacional** solo recibe `input()` y emite `output()`; va en `shared/` si es reutilizable.
- Un servicio por recurso de la API en `core/` o `features/*/`; es el único lugar con `HttpClient`.
- Estado por vista con `ViewState<T>` (`loading | empty | error | ready`) en `shared/`; la plantilla cubre los 4 estados siempre ([spec/06](../spec/06-frontend-angular.md) punto 5).
- Filtros y página en **query params** de la ruta (`?name=&status=&page=`): recargar conserva el estado y es testeable.
- Errores: el interceptor convierte `HttpErrorResponse` en `ApiError` ([formato-error.md](formato-error.md)); las páginas muestran `error.message`.
- Bootstrap: clases utilitarias y componentes CSS (`card`, `form-control`, `btn`, `alert`, `spinner-border`, `pagination`). Sin CSS custom salvo lo imprescindible.
- Reglas de escritura (TypeScript, componentes, plantillas, accesibilidad, signals, formularios, servicios, reglas por versión, lista de prohibido): skill [angular-spa](../skills/angular-spa/SKILL.md) y su hoja por tipo de fichero ([ADR-007](../decisions/ADR-007-best-practices-angular.md)). Este fichero no las repite.

Relacionado: [nomenclatura.md](nomenclatura.md), [references/angular-bootstrap.md](../references/angular-bootstrap.md), [references/testing-frontend.md](../references/testing-frontend.md).
