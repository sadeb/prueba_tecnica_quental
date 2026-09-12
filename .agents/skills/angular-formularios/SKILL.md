---
name: angular-formularios
description: Hoja de angular-spa para los formularios del frontend (login, registro, filtros). Signal Forms si Angular ≥ 22, formularios reactivos tipados si no; validación, feedback Bootstrap, estado submitting, debounce en filtros y accesibilidad de campos. Cargar al crear o editar cualquier formulario.
---

# angular-formularios

Hoja de [angular-spa](../angular-spa/SKILL.md) (tabla por versión y lista de prohibido allí). Formularios del proyecto: login y registro ([wf 13](../../workflows/13-frontend-auth.md)), filtros de personajes ([wf 14](../../workflows/14-frontend-personajes.md)).

## Elegir la API
1. **Angular ≥ 22 → Signal Forms** (`@angular/forms/signals`): estado del formulario en signals, acceso tipado a cada campo y validación por esquema. Guía: [signal-forms.md](../../references/angular-oficial/signal-forms.md) (larga; leer solo la sección necesaria).
2. **Angular < 22 → formularios reactivos** (`ReactiveFormsModule`): `FormBuilder.nonNullable`, `FormGroup` tipado, validadores de `Validators`. Guía: [reactive-forms.md](../../references/angular-oficial/reactive-forms.md).
3. Nunca template-driven.

## Reglas comunes
- El modelo del formulario vive en el componente (página o presentacional `character-filters`); el envío llama a un servicio.
- Validadores declarados junto al campo (`required`; `minLength(8)` en password según [ADR-005](../../decisions/ADR-005-autenticacion.md)). Mensajes con `is-invalid` / `invalid-feedback` de Bootstrap, solo tras tocar el campo o intentar enviar.
- Estado `submitting` en un `signal`; botón deshabilitado mientras está activo; el `ApiError.message` del backend se muestra en `error-alert`.
- Filtros: `debounce` en `name`; el presentacional emite con `output()` y la página escribe en query params y reinicia `page=0`.
- `label` asociado a cada `input` y `autocomplete` adecuado (`username`, `current-password`, `new-password`).
