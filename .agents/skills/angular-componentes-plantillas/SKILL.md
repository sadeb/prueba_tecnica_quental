---
name: angular-componentes-plantillas
description: Reglas oficiales de Angular para componentes y plantillas en este proyecto: input()/output()/model(), host en el decorador, imports explícitos sin CommonModule, control flow nativo, bindings de class/style en lugar de ngClass/ngStyle, NgOptimizedImage y accesibilidad AXE / WCAG AA. Cargar al crear o editar cualquier componente o plantilla del frontend.
---

# angular-componentes-plantillas

Parte de [angular-best-practices](../angular-best-practices/SKILL.md). Separación contenedor / presentacional y carpetas: [conventions/angular.md](../../conventions/angular.md).

## Componente
- Pequeño y con una sola responsabilidad. Si una página crece, extraer presentacionales a `shared/` o a su feature.
- `input()` y `output()`; nunca los decoradores `@Input()` / `@Output()`. Doble enlace `[(prop)]` con `model()`, no con pareja `input()` + `output()`.
- Estado derivado con `computed()`; estado sincronizado con varias fuentes con `linkedSignal()` ([angular-signals-estado](../angular-signals-estado/SKILL.md)).
- Bindings y listeners del host en la propiedad `host` del decorador `@Component` / `@Directive`; nunca `@HostBinding` / `@HostListener`.
- `imports` solo con las directivas y pipes que usa la plantilla (`DatePipe`, `AsyncPipe`, `RouterLink`, `ReactiveFormsModule`…); nunca `CommonModule`.
- Plantilla inline (`template:`) en componentes pequeños (`loading-spinner`, `empty-state`, `favorite-toggle`). Si es externa, `templateUrl` / `styleUrl` con rutas relativas al fichero `.ts`.
- `standalone: true` y `OnPush`: según la versión instalada, tabla en [angular-best-practices](../angular-best-practices/SKILL.md#reglas-que-dependen-de-la-versión).

## Plantilla
- Simple: sin lógica compleja. Los cálculos van a un `computed()` del componente o a un pipe.
- Control flow nativo `@if`, `@for` (siempre con `track`), `@switch`; nunca `*ngIf`, `*ngFor`, `*ngSwitch`.
- `[class.x]` / `[class]` y `[style.x]`; nunca `ngClass` ni `ngStyle`.
- Observables en plantilla con el pipe `async` (importar `AsyncPipe`); sin `subscribe` manual para pintar.
- No asumir globales dentro de la plantilla (`new Date()`, `window`, `localStorage`); calcular en el componente o en un servicio.
- Imágenes estáticas (logo, placeholder) con `NgOptimizedImage` (`ngSrc`); no sirve para base64 inline. Las imágenes de personajes (URL devuelta por la API) llevan `alt` y `loading="lazy"` ([wf 14](../../workflows/14-frontend-personajes.md)).

## Accesibilidad (obligatoria)
- Debe pasar todas las comprobaciones de AXE (las ejecuta el humano; el agente entrega el marcado correcto).
- Mínimos WCAG AA: gestión del foco (tras navegar, al mostrar `error-alert`, al cerrar diálogos), contraste de color (no dar por buenas todas las utilidades de color de Bootstrap) y atributos ARIA correctos y no redundantes.
- Detalle del proyecto: `label` asociado a cada `input`, `aria-live` en `error-alert`, botones con texto visible ([conventions/angular.md](../../conventions/angular.md)).
