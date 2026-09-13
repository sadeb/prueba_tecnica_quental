# ADR-010 · Frontend: entrada única por `/login`, temas, idioma y tratamiento de errores

**Estado**: Propuesta
**Requisito**: [spec/06](../spec/06-frontend-angular.md) puntos 2, 4 y 5; petición del candidato (entrada única sin sesión, tema claro/oscuro, textos en español)
**Fecha**: 2026-09-13

## Contexto
La SPA debe tener una sola pantalla accesible sin sesión, enviar el token en cada petición, responder de forma controlada a todo fallo del backend, alternar tema claro/oscuro y mostrarse en español sin traducir los datos de la API de Rick and Morty. Angular 22.1 (zoneless, Signal Forms estables, `@Service`) y Bootstrap 5.3.8 solo CSS.

## Decisión
- **`/login` es la única ruta pública** (`guestGuard`). El registro es una pestaña dentro de la misma página (`/login?mode=register`); tras registrarse se inicia sesión automáticamente. Todo lo demás cuelga de un layout (`ShellPage`) con `authGuard` + `canActivateChild`, incluido el 404.
- **Sesión**: `Session {token, username, expiresAt}` persistida por `TokenStorage`; la caducidad se comprueba en cada navegación (`sessionStatus()`), y el guard redirige con `returnUrl` y `reason=expired`. `returnUrl` solo se acepta si es una ruta interna.
- **Interceptores funcionales** en orden `auth → retry → error`: bearer solo hacia `apiBaseUrl` y nunca a `/api/auth/*`; un reintento automático de `GET` ante fallo transitorio (red, 502/503/504); timeout de 15 s; todo `HttpErrorResponse` se convierte en `ApiError` (cuerpo del backend, cuerpo no JSON o error de red) con `userMessage` en español; 401 fuera de `/api/auth` → `logout()` + `/login?reason=expired`.
- **Mensajes**: tabla por código/estado más matices por contexto (`login`, `register`, `character`, `related`, `favorites`) con vocabulario de la serie sin perder claridad; los `details` de validación javax se traducen por patrón. `ErrorHandler` global muestra en un toast lo que nadie capturó.
- **Idioma**: interfaz en español; los enumerados cerrados de la API propia (`status`, `gender`) se muestran traducidos en filtros y etiquetas; nombres, especies, tipos, localizaciones y episodios se muestran tal cual llegan.
- **Tema**: `ThemeService` sobre `data-bs-theme` con preferencia persistida y fallback a `prefers-color-scheme`; `index.html` aplica el tema antes de arrancar Angular para evitar parpadeo. Paleta propia vía variables CSS (sin SCSS: el proyecto usa CSS, Bootstrap se importa minificado desde `angular.json`).
- **Almacenamiento**: `BrowserStorage` es el único acceso a `localStorage` (envuelto en try/catch); `TokenStorage` y `ThemeService` lo usan. Generaliza la regla "solo `TokenStorage`" de [angular-spa](../skills/angular-spa/SKILL.md).
- **Modelos** de personaje en `shared/models/` porque `character-card` (shared) los necesita; `features/characters` conserva servicio, filtros y páginas.
- **Estado**: signals y `ViewState<T>` en cada vista; favoritos con actualización optimista y reversión; RxJS solo en la frontera HTTP y en la orquestación de `queryParamMap` → servicio.

## Alternativas descartadas
- Ruta `/register` separada — rompe la exigencia de una sola entrada sin sesión.
- Manejo de errores en cada página — duplica lógica; la conversión centralizada en el interceptor deja a las páginas solo el `describeError(error, contexto)`.
- Bootstrap JS / ng-bootstrap para navbar y toasts — dependencia extra; el colapso y los toasts se resuelven con signals y clases CSS.
- Formularios reactivos — Angular ≥ 22: Signal Forms según [ADR-007](ADR-007-best-practices-angular.md).
- Traducir también nombres o especies — contradice el requisito de mostrar los datos de la API sin alterar.

## Consecuencias
Una sola superficie pública, contrato de error único y previsible, y cada fallo (red, timeout, 4xx, 5xx, sesión caducada, imagen rota, id inválido) tiene una respuesta visible en español. Coste: `errorInterceptor` y `error-messages.ts` son el punto que hay que mantener cuando el backend añada códigos. Defensa en entrevista: "el navegador nunca ve un estado indeterminado: cada `HttpErrorResponse` se convierte una vez en `ApiError` y cada vista renderiza sus cuatro estados".

## Dónde se aplica
`projects/frontend/src/app/core/**` (auth, errors, theme, storage, layout), `features/auth/login.page`, `app.routes.ts`, `app.config.ts`; workflows [12](../workflows/12-frontend-esqueleto.md)–[15](../workflows/15-frontend-favoritos.md) y [17](../workflows/17-pruebas-frontend.md).
