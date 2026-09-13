---
name: angular-spa
description: Entrada única para escribir el frontend Angular standalone con Bootstrap en este proyecto. Buenas prácticas oficiales (angular.dev) adaptadas, TypeScript estricto, reglas que dependen de la versión instalada, lista de prohibiciones e índice de las cuatro hojas por tipo de fichero (componentes-plantillas, signals-estado, formularios, servicios-di). Cargar antes de tocar cualquier fichero de projects/frontend/.
---

# angular-spa

Fuente de las buenas prácticas: <https://angular.dev/assets/context/best-practices.md> (integrada el 2026-09-12). Adopción y conflictos resueltos en [ADR-007](../../decisions/ADR-007-best-practices-angular.md).

## Antes de escribir
1. [conventions/angular.md](../../conventions/angular.md) (carpetas, contenedor / presentacional) y [references/angular-bootstrap.md](../../references/angular-bootstrap.md) (versión instalada, Bootstrap, rutas, Docker).
2. Contrato que se consume: [conventions/api-rest.md](../../conventions/api-rest.md). Errores: [conventions/formato-error.md](../../conventions/formato-error.md).
3. **Solo** la hoja del fichero que se toca:

| Fichero que se toca | Hoja |
|---|---|
| `*.component.ts`, `*.page.ts` y su plantilla | [angular-componentes-plantillas](../angular-componentes-plantillas/SKILL.md) |
| `ViewState`, sesión, favoritos, cualquier `signal` / `computed` | [angular-signals-estado](../angular-signals-estado/SKILL.md) |
| Login, registro, filtros (`character-filters`) | [angular-formularios](../angular-formularios/SKILL.md) |
| `*.service.ts`, interceptores, guardas, `app.config.ts` | [angular-servicios-di](../angular-servicios-di/SKILL.md) |

Las hojas no se enlazan entre sí: si una tarea toca dos tipos de fichero, se cargan dos hojas. Documentación oficial ampliada, solo si la hoja no basta: [references/angular-oficial/](../../references/angular-oficial/README.md).

## TypeScript y arquitectura
- `strict: true`. Nunca `any`; `unknown` y estrechar (`typeof`, guardas de tipo) cuando el tipo es incierto. Preferir la inferencia; anotar el tipo de retorno de los métodos públicos de servicios.
- Solo componentes standalone; nunca NgModules.
- Rutas de feature con carga perezosa (`loadComponent` / `loadChildren`) en `app.routes.ts`. Filtros y página en query params (`?name=&status=&page=`).
- Sin paquetes npm nuevos sin ADR. Sin CSS custom más allá de utilidades Bootstrap. Nunca llamar a `rickandmortyapi.com` desde el navegador.

## Reglas que dependen de la versión
Versión exacta anotada en [references/angular-bootstrap.md](../../references/angular-bootstrap.md) tras `ng new`. No asumir Angular 22 hasta comprobarlo.

| Regla | Aplica desde | Si la versión es menor |
|---|---|---|
| No escribir `standalone: true` (es el valor por defecto) | v19 | Escribirlo en cada componente, directiva y pipe |
| No escribir `changeDetection: ChangeDetectionStrategy.OnPush` (es el valor por defecto) | v22 | Escribirlo en **todos** los componentes |
| Signal Forms (`@angular/forms/signals`) en formularios nuevos | v22 | Formularios reactivos tipados |
| `@Service` en lugar de `@Injectable({ providedIn: 'root' })` | v22 | `@Injectable({ providedIn: 'root' })` |

Todo lo demás (signals, `input()` / `output()`, control flow nativo, `inject()`, `host`, accesibilidad) aplica en cualquier versión soportada.

## Prohibido
Lista única; las hojas y las convenciones no la repiten.

`any` · `CommonModule` · `ngClass` / `ngStyle` · `*ngIf` / `*ngFor` / `*ngSwitch` · `@Input` / `@Output` / `@HostBinding` / `@HostListener` · inyección por constructor · clases `implements HttpInterceptor` / `CanActivate` · `FormsModule` / `ngModel` · `mutate()` o mutar el objeto interno de un signal · `BehaviorSubject` como estado · `subscribe` sin `takeUntilDestroyed()` · `localStorage` fuera de `TokenStorage` · NgRx u otra librería de estado.

## Pruebas y entrega
Pruebas: [testing-aislado](../testing-aislado/SKILL.md). Entrega: ficheros tocados, comandos para el humano (`npm ci`, `npx ng build`, `npx ng test --watch=false`) y commit propuesto ([no-run-commands](../no-run-commands/SKILL.md)).
