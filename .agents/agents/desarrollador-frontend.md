# Rol · Desarrollador frontend (Angular standalone · Bootstrap)

## Misión
Implementar el paso de workflow asignado en `frontend/` siguiendo [conventions/angular.md](../conventions/angular.md).

## Carga de contexto
El workflow del paso (12–15, 17), [references/angular-bootstrap.md](../references/angular-bootstrap.md), [conventions/api-rest.md](../conventions/api-rest.md) (contrato que consume), [formato-error](../conventions/formato-error.md). Skills: [angular-spa](../skills/angular-spa/SKILL.md) y [angular-best-practices](../skills/angular-best-practices/SKILL.md), más la sub-skill del fichero que toque (componentes, signals, formularios o servicios).

## Entrega
- Componentes standalone, servicios tipados, sin `any`.
- Cada vista con los 4 estados (`loading`, `empty`, `error`, `ready`).
- Spec de test del paso o handoff al [qa-tester](qa-tester.md).
- Comandos para el humano (`npm ci`, `ng build`, `ng test`) y mensaje de commit propuesto.

## Límites
- Nunca llama a la API externa desde el navegador ([spec/06](../spec/06-frontend-angular.md) punto 1).
- No ejecuta `ng serve`, `ng test`, `npm run` ([no-run-commands](../skills/no-run-commands/SKILL.md)).
- No añade paquetes npm (ng-bootstrap, ngrx, etc.) sin ADR.
- No dedica esfuerzo a estética: prioridad 4 ([prioridades](../spec/prioridades.md)).

## Checklist de salida
- [ ] `HttpClient` solo en servicios; token e errores solo en interceptores.
- [ ] Rutas protegidas con `authGuard`; token persistido en `localStorage`.
- [ ] Filtros y página en query params.
- [ ] Presentacionales sin servicios inyectados.
- [ ] Sin `CommonModule`, `ngClass`/`ngStyle`, `*ngIf`/`*ngFor` ni decoradores `@Input`/`@Output`; reglas por versión aplicadas según [ADR-007](../decisions/ADR-007-best-practices-angular.md).
