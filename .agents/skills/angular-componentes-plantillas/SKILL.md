---
name: angular-componentes-plantillas
description: Hoja de angular-spa para componentes y plantillas. Página frente a presentacional, input()/output()/model(), host en el decorador, imports explícitos, control flow nativo con los cuatro estados de vista, bindings de class/style, NgOptimizedImage y accesibilidad AXE / WCAG AA. Cargar al crear o editar cualquier .component.ts, .page.ts o plantilla del frontend.
---

# angular-componentes-plantillas

Hoja de [angular-spa](../angular-spa/SKILL.md) (tabla por versión y lista de prohibido allí). Carpetas: [conventions/angular.md](../../conventions/angular.md).

## Componente
- Pequeño y con una sola responsabilidad. Si una página crece, extraer presentacionales a `shared/` o a su feature.
- **Página (contenedor)**: inyecta servicios, mantiene el `ViewState<T>` de la vista en un `signal`, lee filtros de `ActivatedRoute.queryParamMap` y escribe con `router.navigate([], { queryParams, queryParamsHandling: 'merge' })`.
- **Presentacional**: solo `input()`, `output()` y `model()` (doble enlace `[(prop)]`); sin servicios de datos inyectados.
- Estado derivado con `computed()`; nunca un segundo `signal` para lo que se puede calcular.
- Bindings y listeners del host en la propiedad `host` del decorador `@Component` / `@Directive`.
- `imports` solo con las directivas y pipes que usa la plantilla (`DatePipe`, `AsyncPipe`, `RouterLink`, `ReactiveFormsModule`…).
- Plantilla inline (`template:`) en componentes pequeños (`loading-spinner`, `empty-state`, `favorite-toggle`). Si es externa, `templateUrl` / `styleUrl` con rutas relativas al `.ts`.
- `standalone: true` y `OnPush`: según la tabla por versión de angular-spa.

## Plantilla
- Simple: los cálculos van a un `computed()` del componente o a un pipe. No usar globales (`new Date()`, `window`, `localStorage`) en la plantilla.
- Control flow nativo `@if`, `@for` (siempre con `track`), `@switch`.
- Los cuatro estados de vista: `@if (state().status === 'loading')` spinner Bootstrap; `'empty'` mensaje; `'error'` `error-alert` con output `retry`; `'ready'` contenido.
- `[class.x]` / `[class]` y `[style.x]`.
- Observables en plantilla con `AsyncPipe`; sin `subscribe` manual para pintar.
- Imágenes estáticas (logo, placeholder) con `NgOptimizedImage` (`ngSrc`); no sirve para base64 inline. Imágenes de personajes (URL devuelta por la API) con `alt` y `loading="lazy"`.

## Accesibilidad (obligatoria)
- Debe pasar todas las comprobaciones de AXE (las ejecuta el humano; el agente entrega el marcado correcto).
- Mínimos WCAG AA: gestión del foco (tras navegar, al mostrar `error-alert`, al cerrar diálogos), contraste de color (no dar por buenas todas las utilidades de color de Bootstrap), atributos ARIA correctos y no redundantes.
- `label` asociado a cada `input` y `autocomplete` adecuado; `aria-live` en `error-alert`; botones con texto visible.

Profundizar, solo si hace falta: [components.md](../../references/angular-oficial/components.md), [inputs.md](../../references/angular-oficial/inputs.md).
