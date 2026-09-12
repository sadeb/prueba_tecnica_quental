---
name: angular-best-practices
description: Buenas prácticas oficiales de Angular (angular.dev) adaptadas a este proyecto: TypeScript estricto, standalone, lazy loading y reglas que dependen de la versión instalada. Índice de las skills angular-componentes-plantillas, angular-signals-estado, angular-formularios y angular-servicios-di. Cargar junto a angular-spa antes de escribir frontend.
---

# angular-best-practices

Fuente: <https://angular.dev/assets/context/best-practices.md> (integrada el 2026-09-12 y segmentada por tema). Adopción y conflictos resueltos con las convenciones previas: [ADR-007](../../decisions/ADR-007-best-practices-angular.md). Procedimiento del proyecto: [angular-spa](../angular-spa/SKILL.md).

## Skills por tema (cargar solo la del fichero que se toca)
| Skill | Cuándo |
|---|---|
| [angular-componentes-plantillas](../angular-componentes-plantillas/SKILL.md) | Cualquier `.component.ts` / `.page.ts` y su plantilla; accesibilidad |
| [angular-signals-estado](../angular-signals-estado/SKILL.md) | Estado local, derivado o compartido; `ViewState`, sesión, favoritos |
| [angular-formularios](../angular-formularios/SKILL.md) | Login, registro, filtros de personajes |
| [angular-servicios-di](../angular-servicios-di/SKILL.md) | Servicios, `inject()`, interceptores y guardas |

## TypeScript
- `strict: true` en `tsconfig` ([conventions/angular.md](../../conventions/angular.md)).
- Preferir la inferencia cuando el tipo es obvio; anotar siempre el tipo de retorno de los métodos públicos de servicios.
- Nunca `any`; `unknown` cuando el tipo es incierto, y estrechar (`typeof`, guardas de tipo) antes de usar.

## Arquitectura
- Siempre componentes standalone; nunca NgModules.
- Rutas de feature con carga perezosa (`loadComponent` / `loadChildren`) en `app.routes.ts` ([references/angular-bootstrap.md](../../references/angular-bootstrap.md)).

## Reglas que dependen de la versión
Tras `ng new`, anotar la versión exacta en [references/angular-bootstrap.md](../../references/angular-bootstrap.md) y aplicar esta tabla.

| Regla | Aplica desde | Si la versión es menor |
|---|---|---|
| No escribir `standalone: true` en decoradores (es el valor por defecto) | v19 (la fuente lo fija en v20+) | Escribirlo en cada componente, directiva y pipe |
| No escribir `changeDetection: ChangeDetectionStrategy.OnPush` (es el valor por defecto) | v22 | Escribirlo en **todos** los componentes |
| Signal Forms (`@angular/forms/signals`) para formularios nuevos | v22 (estables) | Formularios reactivos ([angular-formularios](../angular-formularios/SKILL.md)) |
| `@Service` en lugar de `@Injectable({ providedIn: 'root' })` | v22 | `@Injectable({ providedIn: 'root' })` ([angular-servicios-di](../angular-servicios-di/SKILL.md)) |

Todo lo demás (signals, `input()`/`output()`, control flow nativo, `inject()`, `host`, accesibilidad) aplica en cualquier versión soportada.
