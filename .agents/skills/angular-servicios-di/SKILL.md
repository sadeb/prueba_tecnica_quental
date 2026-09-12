---
name: angular-servicios-di
description: Hoja de angular-spa para servicios, inyección de dependencias, interceptores y guardas. Una responsabilidad por servicio, singleton en root (@Service o @Injectable providedIn root según versión), inject() en campos, HttpClient solo en servicios, HttpInterceptorFn y CanActivateFn registrados en app.config.ts. Cargar al crear o editar *.service.ts, interceptores, guardas o app.config.ts.
---

# angular-servicios-di

Hoja de [angular-spa](../angular-spa/SKILL.md) (tabla por versión y lista de prohibido allí). Carpetas `core/` y `features/*/*.service.ts`: [conventions/angular.md](../../conventions/angular.md).

## Servicios
- Una responsabilidad por servicio: `CharacterService` (HTTP de `/api/characters*`), `FavoriteService` (HTTP de favoritos + set de ids), `AuthService` (sesión; lee el token al arrancar), `TokenStorage` (único acceso a `localStorage`). No mezclar HTTP con navegación ni con UI.
- Singleton en root con el decorador que marque la tabla por versión. Sin `providers: []` en componentes salvo necesidad justificada.
- `inject()` en campos de clase:
  ```ts
  private readonly http = inject(HttpClient);
  ```
- El servicio es el único lugar con `HttpClient`. Los métodos devuelven `Observable<T>` tipado sobre `environment.apiBaseUrl`; params solo si tienen valor.
- Estado compartido dentro del servicio con signals.

## Interceptores y guardas
- Funcionales (`HttpInterceptorFn`, `CanActivateFn`) con `inject()` dentro de la función; registrados en `app.config.ts` con `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))` y `provideRouter(routes)`.
- `authInterceptor`: añade `Authorization: Bearer <token>` si hay token.
- `errorInterceptor`: convierte `HttpErrorResponse` en `ApiError` ([conventions/formato-error.md](../../conventions/formato-error.md)); en 401 llama `AuthService.logout()` y navega a `/login`.
- `authGuard`: devuelve `true` o `router.createUrlTree(['/login'], { queryParams: { returnUrl } })`.

Profundizar, solo si hace falta: [creating-services.md](../../references/angular-oficial/creating-services.md), [http-client.md](../../references/angular-oficial/http-client.md).
