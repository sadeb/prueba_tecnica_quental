# Pruebas frontend (Angular)

Requisito: al menos una prueba sobre lógica no trivial ([spec/07-pruebas.md](../spec/07-pruebas.md)).

- Runner: el que genere el CLI de la versión elegida (Jasmine/Karma en versiones anteriores; Vitest en las recientes). No cambiarlo: economía de dependencias. Anotar el runner en [angular-bootstrap.md](angular-bootstrap.md).
- **Servicios HTTP**: `provideHttpClientTesting()` + `HttpTestingController`: verificar URL, query params de filtros/paginación, mapeo de respuesta, propagación de error.
- **Interceptor**: petición con token → cabecera `Authorization`; respuesta 401 → `AuthService.logout()` y navegación a `/login`.
- **Guarda**: `authGuard` con `AuthService` falso: autenticado → `true`; no autenticado → `UrlTree` a `/login` con `returnUrl`.
- **Componente con filtros**: cambiar un filtro → llamada al servicio con los parámetros correctos y reset de página a 1; estados `loading`/`empty`/`error` renderizan lo esperado.
- Sin llamadas reales a red; sin `setTimeout` reales (usar `fakeAsync`/`tick` o `await fixture.whenStable()`).
- Candidatas mínimas (elegir ≥ 1, ideal 3): `auth.guard.spec.ts`, `auth.interceptor.spec.ts`, `character-list.component.spec.ts`.

El agente **no ejecuta** `ng test` ([no-run-commands](../skills/no-run-commands/SKILL.md)).

Profundizar, solo si hace falta: [angular-oficial/testing-fundamentals.md](angular-oficial/testing-fundamentals.md), [angular-oficial/router-testing.md](angular-oficial/router-testing.md).

Relacionado: [workflows/17-pruebas-frontend.md](../workflows/17-pruebas-frontend.md), [conventions/angular.md](../conventions/angular.md).
