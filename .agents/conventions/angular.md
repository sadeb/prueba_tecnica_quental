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
- **Página (contenedor)** = orquesta servicios y estado; sufijo `.page.ts` o `-page.component.ts`. **Presentacional** = solo `@Input`/`@Output` (o `input()`/`output()`), sin inyectar servicios de datos.
- Un servicio por recurso de la API; el único lugar con `HttpClient`. Devuelven `Observable<T>` tipado; sin `any`.
- Estado por vista con un objeto `ViewState<T>` (`loading | empty | error | ready`) en signal; la plantilla cubre los 4 estados siempre ([spec/06](../spec/06-frontend-angular.md) punto 5).
- Filtros y página en **query params** de la ruta (`?name=&status=&page=`): recargar conserva el estado y es testeable.
- Errores: el interceptor convierte `HttpErrorResponse` en `ApiError` ([formato-error.md](formato-error.md)); las páginas muestran `error.message`; 401 lo resuelve el interceptor (logout + redirect).
- Bootstrap: clases utilitarias y componentes CSS (`card`, `form-control`, `btn`, `alert`, `spinner-border`, `pagination`). Sin CSS custom salvo lo imprescindible.
- Sin `any`, `strict: true` en `tsconfig`. Sin `subscribe` anidados; `takeUntilDestroyed()` o `async`/signals.
- Accesibilidad básica: `label` asociado a inputs, `aria-live` en alertas de error, botones con texto.

Relacionado: [nomenclatura.md](nomenclatura.md), [references/angular-bootstrap.md](../references/angular-bootstrap.md), [references/testing-frontend.md](../references/testing-frontend.md).
