---
name: angular-formularios
description: Reglas para los formularios del frontend (login, registro, filtros): Signal Forms si Angular ≥ 22, formularios reactivos tipados si no; nunca template-driven. Validación, feedback Bootstrap, estado submitting y accesibilidad. Cargar al crear o editar cualquier formulario.
---

# angular-formularios

Parte de [angular-best-practices](../angular-best-practices/SKILL.md). Formularios del proyecto: login y registro ([wf 13](../../workflows/13-frontend-auth.md)), filtros de personajes ([wf 14](../../workflows/14-frontend-personajes.md)).

## Elegir la API (versión anotada en [references/angular-bootstrap.md](../../references/angular-bootstrap.md))
1. **Angular ≥ 22 → Signal Forms** (`@angular/forms/signals`, estables): estado del formulario en signals, acceso tipado a cada campo y validación por esquema. Referencia oficial: <https://angular.dev/guide/forms/signal-forms>.
2. **Angular < 22 → formularios reactivos** (`ReactiveFormsModule`): `FormBuilder.nonNullable`, `FormGroup` tipado, validadores de `Validators`.
3. **Nunca** template-driven (`FormsModule`, `ngModel`).

## Reglas comunes
- El modelo del formulario vive en el componente (página o presentacional `character-filters`); el envío llama a un servicio ([angular-servicios-di](../angular-servicios-di/SKILL.md)).
- Validadores declarados junto al campo (`required`; `minLength(8)` en password según [ADR-005](../../decisions/ADR-005-autenticacion.md)). Mensajes con `is-invalid` / `invalid-feedback` de Bootstrap, solo tras tocar el campo o intentar enviar.
- Estado `submitting` en un `signal` ([angular-signals-estado](../angular-signals-estado/SKILL.md)); botón deshabilitado mientras está activo; el `ApiError.message` del backend se muestra en `error-alert` ([conventions/formato-error.md](../../conventions/formato-error.md)).
- Filtros: `debounce` en `name`; el presentacional emite con `output()` y la página escribe en query params ([angular-spa](../angular-spa/SKILL.md)).
- `label` asociado a cada `input` y `autocomplete` adecuado (`username`, `current-password`, `new-password`) ([angular-componentes-plantillas](../angular-componentes-plantillas/SKILL.md#accesibilidad-obligatoria)).
- Pruebas: [references/testing-frontend.md](../../references/testing-frontend.md).
