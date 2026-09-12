---
name: angular-signals-estado
description: Hoja de angular-spa para el estado con signals. signal() local y compartido, computed() derivado, linkedSignal() sincronizado con varias fuentes, model() para doble enlace, set/update sin mutación, ViewState<T> del proyecto, actualización optimista de favoritos y frontera con RxJS. Cargar al manejar ViewState, filtros, sesión o favoritos.
---

# angular-signals-estado

Hoja de [angular-spa](../angular-spa/SKILL.md) (lista de prohibido allí). Sin NgRx ni librerías de estado.

## Reglas
- Signals para todo el estado: local del componente (`ViewState<T>`, `submitting`) y compartido en servicios (`isAuthenticated`, set de ids favoritos).
- `computed()` para lo derivado: `isAuthenticated` a partir del token, `isFavorite(id)` a partir del set, `hasResults` a partir del `ViewState`.
- `linkedSignal()` para estado que depende de varias fuentes reactivas y debe reiniciarse cuando cambian (p. ej. una selección que vuelve a `null` al recargar la lista).
- `model()` para propiedades de doble enlace en presentacionales.
- Cambiar estado solo con `set()` o `update()`. Objetos y colecciones se reemplazan: `update(s => ({ ...s, status: 'ready' }))`, `update(ids => new Set(ids).add(id))`.
- Transformaciones puras: sin efectos laterales en `computed()`. Las llamadas HTTP viven en el servicio y escriben el resultado con `set()`.
- Frontera con RxJS: `toSignal()` (`@angular/core/rxjs-interop`) o `subscribe` con `takeUntilDestroyed()`; en plantilla, `AsyncPipe`.

## `ViewState<T>` en este proyecto
Modelo `{ status: 'loading' | 'empty' | 'error' | 'ready', data?, error? }` en `shared/`. `signal<ViewState<T>>({ status: 'loading' })` → al responder el servicio, `set({ status: 'ready', data })` o `set({ status: 'empty' })`; en error, `set({ status: 'error', error })`. La plantilla cubre los cuatro estados. Actualización optimista de favoritos: `update()` antes de la llamada y `update()` inverso en el `error` ([wf 15](../../workflows/15-frontend-favoritos.md)).

Profundizar, solo si hace falta: [signals-overview.md](../../references/angular-oficial/signals-overview.md), [linked-signal.md](../../references/angular-oficial/linked-signal.md).
