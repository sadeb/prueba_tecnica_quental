# ADR-007 · Adopción de las buenas prácticas oficiales de Angular

**Estado**: Propuesta · **Requisito**: [spec/06](../spec/06-frontend-angular.md) ("uso idiomático del framework"), [spec/09](../spec/09-criterios-valoracion.md) · **Fecha**: 2026-09-12

## Contexto
La prueba valora el uso idiomático de Angular. El equipo de Angular publica un fichero de contexto para agentes (<https://angular.dev/assets/context/best-practices.md>) cuyas reglas dependen en parte de la versión (v20+, v22+), y la versión del proyecto se fija tras `ng new` ([references/stack-versiones.md](../references/stack-versiones.md)). Algunas reglas chocaban con las convenciones iniciales.

## Decisión
- Adoptar íntegramente el fichero oficial, segmentado en una skill de entrada y cuatro hojas: [angular-spa](../skills/angular-spa/SKILL.md) (procedimiento, TypeScript, arquitectura, tabla por versión, lista única de prohibido e índice de hojas), [angular-componentes-plantillas](../skills/angular-componentes-plantillas/SKILL.md), [angular-signals-estado](../skills/angular-signals-estado/SKILL.md), [angular-formularios](../skills/angular-formularios/SKILL.md) y [angular-servicios-di](../skills/angular-servicios-di/SKILL.md). Cada regla vive en un solo fichero; las hojas no se enlazan entre sí y `conventions/` y `references/` solo apuntan a ellas.
- La documentación oficial ampliada (skill `angular-developer` del equipo de Angular) se conserva como extracto de 13 references en [references/angular-oficial/](../references/angular-oficial/README.md), sin `SKILL.md`, enlazada solo desde las hojas.
- Las reglas dependientes de versión se aplican según la versión real anotada en [references/angular-bootstrap.md](../references/angular-bootstrap.md); no se asume Angular 22 hasta comprobarlo.
- La accesibilidad pasa a requisito: AXE sin errores y mínimos WCAG AA.

Cambios sobre las convenciones previas:

| Antes | Ahora |
|---|---|
| `@Input` / `@Output` o `input()` / `output()` | Solo `input()` / `output()`; `model()` para doble enlace |
| `OnPush` explícito en presentacionales | Sin declarar si Angular ≥ 22; en todos los componentes si < 22 |
| Formularios reactivos | Signal Forms si Angular ≥ 22; reactivos si no |
| `@Injectable({ providedIn: 'root' })` | `@Service` si Angular ≥ 22 |
| Estado con `signal()` o `BehaviorSubject` | Solo signals; RxJS únicamente en la frontera HTTP |
| No regulado | Prohibidos `CommonModule`, `ngClass`, `ngStyle`, `@HostBinding`, `@HostListener`, `*ngIf` / `*ngFor` / `*ngSwitch` |

## Alternativas descartadas
- Mantener las convenciones iniciales e ignorar el fichero — pierde puntos en "uso idiomático" y en entrevista es difícil justificar decoradores y sintaxis legacy.
- Copiar el fichero tal cual en `references/` — un bloque sin adaptar al proyecto, sin resolver los conflictos de versión y contra el principio "un fichero = un concepto".
- Instalar completas las skills oficiales `angular-developer` (44 ficheros, ~40 000 tokens) y `angular-new-app` — su índice ordena ejecutar `ng build` / `ng generate` y asumir la última versión, contra [no-run-commands](../skills/no-run-commands/SKILL.md) y la tabla por versión; dos tercios de sus references (Aria, Tailwind, SSR, animaciones, MCP) no aplican al proyecto.
- Mantener `angular-best-practices` como skill separada de `angular-spa` — siempre se cargaban juntas y el checklist de una repetía las reglas de la otra.
- Fijar ya Angular 22 para aplicar todas las reglas — la versión se decide al crear el proyecto; se prefiere la tabla condicional.

## Consecuencias
Código alineado con la documentación oficial y defendible en una frase: "seguimos el fichero de contexto que publica el propio equipo de Angular, adaptado a la versión instalada". Coste: cuatro hojas más; por tarea se carga `angular-spa` y solo la hoja del fichero que se toca, unos 1 000 + 600 tokens ([economia-tokens](../skills/economia-tokens/SKILL.md)). AXE y `ng build` los ejecuta el humano ([no-run-commands](../skills/no-run-commands/SKILL.md)). Vigilar: si la versión instalada es < 22, no usar `@Service` ni Signal Forms aunque las skills los mencionen.

## Dónde se aplica
[wf 12](../workflows/12-frontend-esqueleto.md), [wf 13](../workflows/13-frontend-auth.md), [wf 14](../workflows/14-frontend-personajes.md), [wf 15](../workflows/15-frontend-favoritos.md), [wf 17](../workflows/17-pruebas-frontend.md); [conventions/angular.md](../conventions/angular.md); [skills/angular-spa](../skills/angular-spa/SKILL.md).
