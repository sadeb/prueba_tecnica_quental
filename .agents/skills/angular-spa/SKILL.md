---
name: angular-spa
description: Procedimiento para escribir el frontend Angular standalone con Bootstrap en este proyecto: servicios, interceptores, guardas, estados de vista, filtros en query params, sin dependencias extra. Cargar antes de tocar cualquier fichero de frontend/.
---

# angular-spa

## Antes de escribir
Leer [conventions/angular.md](../../conventions/angular.md) y [references/angular-bootstrap.md](../../references/angular-bootstrap.md). Contrato a consumir: [conventions/api-rest.md](../../conventions/api-rest.md).

## Checklist por pieza
- **Servicio**: `@Injectable({ providedIn: 'root' })`, `HttpClient` inyectado con `inject()`, métodos que devuelven `Observable<T>` tipado con `environment.apiBaseUrl`. Sin lógica de UI.
- **Interceptor**: funcional (`HttpInterceptorFn`). `authInterceptor` añade Bearer si hay token; `errorInterceptor` mapea a `ApiError`, en 401 llama `AuthService.logout()` y navega a `/login`.
- **Guarda**: `CanActivateFn` que devuelve `true` o `router.createUrlTree(['/login'], { queryParams: { returnUrl } })`.
- **Página**: inyecta servicios, mantiene `ViewState<T>` en `signal`, lee filtros de `ActivatedRoute.queryParamMap`, escribe con `router.navigate([], { queryParams, queryParamsHandling: 'merge' })`.
- **Presentacional**: solo `input()`/`output()`; sin `inject()` de servicios de datos; `ChangeDetectionStrategy.OnPush`.
- **Plantilla**: `@if (state().status === 'loading')` spinner Bootstrap; `'empty'` mensaje; `'error'` `error-alert` con `retry` output; `'ready'` contenido.
- **Formularios**: `FormBuilder.nonNullable`, validadores, deshabilitar botón mientras `submitting`.

## Prohibido
`any`; llamadas a `rickandmortyapi.com`; `localStorage` fuera de `TokenStorage`; `subscribe` sin cleanup; paquetes npm nuevos sin ADR; CSS custom más allá de utilidades.

## Pruebas
Ver [testing-aislado](../testing-aislado/SKILL.md) y [references/testing-frontend.md](../../references/testing-frontend.md).

## Entrega
Ficheros tocados, comandos para el humano (`npm ci`, `npx ng build`, `npx ng test --watch=false`) y commit propuesto ([no-run-commands](../no-run-commands/SKILL.md)).
