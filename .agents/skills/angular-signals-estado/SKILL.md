---
name: angular-signals-estado
description: Reglas oficiales de Angular para el estado con signals en este proyecto: signal() para estado local y compartido, computed() para derivado, linkedSignal() para estado sincronizado con varias fuentes, model() para doble enlace, set/update en lugar de mutate, transformaciones puras. Cargar al manejar ViewState, filtros, sesión o favoritos.
---

# angular-signals-estado

Parte de [angular-best-practices](../angular-best-practices/SKILL.md). Sin NgRx ni librerías de estado ([references/stack-versiones.md](../../references/stack-versiones.md)).

## Reglas
- Signals para todo el estado: local del componente (`ViewState<T>`, `submitting`) y compartido en servicios (`isAuthenticated`, set de ids favoritos).
- `computed()` para lo derivado: `isAuthenticated` a partir del token, `isFavorite(id)` a partir del set, `hasResults` a partir del `ViewState`. Nunca guardar en un segundo `signal` lo que se puede calcular.
- `linkedSignal()` para estado que depende de varias fuentes reactivas y debe reiniciarse cuando cambian (p. ej. una selección que vuelve a `null` al recargar la lista). Referencia: <https://angular.dev/guide/signals/linked-signal>.
- `model()` para propiedades de doble enlace en presentacionales, en lugar de `input()` + `output()` ([angular-componentes-plantillas](../angular-componentes-plantillas/SKILL.md)).
- Cambiar estado solo con `set()` o `update()`; nunca `mutate()` ni mutar el objeto interno (`state().data.push(...)`). Objetos y colecciones se reemplazan: `update(s => ({ ...s, status: 'ready' }))`, `update(ids => new Set(ids).add(id))`.
- Transformaciones puras y predecibles: sin efectos laterales en `computed()`; las llamadas HTTP viven en el servicio ([angular-servicios-di](../angular-servicios-di/SKILL.md)) y escriben el resultado con `set()`.
- Frontera con RxJS: `toSignal()` (`@angular/core/rxjs-interop`) o `subscribe` con `takeUntilDestroyed()`; en plantilla, pipe `async`.

## `ViewState<T>` en este proyecto
`signal<ViewState<T>>({ status: 'loading' })` → al responder el servicio, `set({ status: 'ready', data })` o `set({ status: 'empty' })`; en error, `set({ status: 'error', error })`. La plantilla cubre los cuatro estados ([conventions/angular.md](../../conventions/angular.md)). Actualización optimista de favoritos: `update()` antes de la llamada y `update()` inverso en el `error` ([wf 15](../../workflows/15-frontend-favoritos.md)).
