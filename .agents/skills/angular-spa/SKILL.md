---
name: angular-spa
description: Procedimiento para escribir el frontend Angular standalone con Bootstrap en este proyecto: servicios, interceptores, guardas, estados de vista, filtros en query params, sin dependencias extra. Cargar antes de tocar cualquier fichero de frontend/.
---

# angular-spa

## Antes de escribir
Leer [conventions/angular.md](../../conventions/angular.md) y [references/angular-bootstrap.md](../../references/angular-bootstrap.md). Contrato a consumir: [conventions/api-rest.md](../../conventions/api-rest.md). Buenas prácticas oficiales: [angular-best-practices](../angular-best-practices/SKILL.md) más la sub-skill del tipo de fichero que se toque ([ADR-007](../../decisions/ADR-007-best-practices-angular.md)).

## Checklist por pieza
- **Servicio**: `@Service` o `@Injectable({ providedIn: 'root' })` según versión ([angular-servicios-di](../angular-servicios-di/SKILL.md)), `HttpClient` inyectado con `inject()`, métodos que devuelven `Observable<T>` tipado con `environment.apiBaseUrl`. Sin lógica de UI.
- **Interceptor**: funcional (`HttpInterceptorFn`). `authInterceptor` añade Bearer si hay token; `errorInterceptor` mapea a `ApiError`, en 401 llama `AuthService.logout()` y navega a `/login`.
- **Guarda**: `CanActivateFn` que devuelve `true` o `router.createUrlTree(['/login'], { queryParams: { returnUrl } })`.
- **Página**: inyecta servicios, mantiene `ViewState<T>` en `signal` ([angular-signals-estado](../angular-signals-estado/SKILL.md)), lee filtros de `ActivatedRoute.queryParamMap`, escribe con `router.navigate([], { queryParams, queryParamsHandling: 'merge' })`.
- **Presentacional**: solo `input()`/`output()`/`model()`; sin `inject()` de servicios de datos; `OnPush` solo si la versión lo exige ([angular-componentes-plantillas](../angular-componentes-plantillas/SKILL.md)).
- **Plantilla**: `@if (state().status === 'loading')` spinner Bootstrap; `'empty'` mensaje; `'error'` `error-alert` con `retry` output; `'ready'` contenido.
- **Formularios**: Signal Forms o reactivos según versión ([angular-formularios](../angular-formularios/SKILL.md)); validadores; deshabilitar botón mientras `submitting`.

## Prohibido
`any`; llamadas a `rickandmortyapi.com`; `localStorage` fuera de `TokenStorage`; `subscribe` sin cleanup; paquetes npm nuevos sin ADR; CSS custom más allá de utilidades; `CommonModule`, `ngClass`/`ngStyle`, `*ngIf`/`*ngFor`, decoradores `@Input`/`@Output`/`@HostBinding`/`@HostListener` ([ADR-007](../../decisions/ADR-007-best-practices-angular.md)).

## Pruebas
Ver [testing-aislado](../testing-aislado/SKILL.md) y [references/testing-frontend.md](../../references/testing-frontend.md).

## Entrega
Ficheros tocados, comandos para el humano (`npm ci`, `npx ng build`, `npx ng test --watch=false`) y commit propuesto ([no-run-commands](../no-run-commands/SKILL.md)).
