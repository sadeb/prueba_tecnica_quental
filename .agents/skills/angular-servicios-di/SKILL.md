---
name: angular-servicios-di
description: Reglas oficiales de Angular para servicios e inyección de dependencias en este proyecto: una responsabilidad por servicio, singleton en root (@Service en Angular ≥ 22 o @Injectable providedIn root), inject() en lugar de constructor; aplicable también a interceptores y guardas funcionales. Cargar al crear o editar servicios, interceptores o guardas.
---

# angular-servicios-di

Parte de [angular-best-practices](../angular-best-practices/SKILL.md). Carpetas `core/` y `features/*/*.service.ts`: [conventions/angular.md](../../conventions/angular.md).

## Servicios
- Una responsabilidad por servicio: `CharacterService` (HTTP de `/api/characters*`), `FavoriteService` (HTTP de favoritos + set de ids), `AuthService` (sesión), `TokenStorage` (solo `localStorage`). No mezclar HTTP con navegación ni con UI.
- Singleton en root: **Angular ≥ 22 → `@Service`**; **< 22 → `@Injectable({ providedIn: 'root' })`** (tabla en [angular-best-practices](../angular-best-practices/SKILL.md#reglas-que-dependen-de-la-versión)). Sin `providers: []` en componentes salvo necesidad justificada.
- `inject()` en campos de clase; nunca inyección por constructor.
  ```ts
  private readonly http = inject(HttpClient);
  ```
- Los métodos HTTP devuelven `Observable<T>` tipado sobre `environment.apiBaseUrl`; sin `any` ([angular-best-practices](../angular-best-practices/SKILL.md#typescript)).
- Estado compartido dentro del servicio con signals ([angular-signals-estado](../angular-signals-estado/SKILL.md)).

## Interceptores y guardas
- Funcionales (`HttpInterceptorFn`, `CanActivateFn`) con `inject()` dentro de la función; registrados en `app.config.ts` con `provideHttpClient(withInterceptors([...]))` y `provideRouter(...)`.
- Sin clases `implements HttpInterceptor` / `CanActivate` ni `HTTP_INTERCEPTORS`.
- Comportamiento de cada uno: [angular-spa](../angular-spa/SKILL.md#checklist-por-pieza), [references/angular-bootstrap.md](../../references/angular-bootstrap.md).
