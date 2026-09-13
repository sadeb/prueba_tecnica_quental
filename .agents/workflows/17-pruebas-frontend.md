# 17 · Pruebas frontend

**Rol**: [qa-tester](../agents/qa-tester.md) · Skill: [testing-aislado](../skills/testing-aislado/SKILL.md)

## Objetivo
Cumplir [spec/07](../spec/07-pruebas.md) punto 4 y, si hay tiempo, bonus B5.

## Contexto
[references/testing-frontend.md](../references/testing-frontend.md), [conventions/angular.md](../conventions/angular.md).

## Inventario
| Spec | Prioridad | Cubre |
|---|---|---|
| `auth.guard.spec.ts` | obligatoria (elegir ≥ 1 de las tres) | autenticado → true; no → UrlTree `/login` con `returnUrl` |
| `auth.interceptor.spec.ts` / `error.interceptor.spec.ts` | obligatoria | cabecera Bearer; 401 → logout + `/login`; mapeo `ApiError` |
| `character-list.page.spec.ts` | obligatoria | filtro → params y `page=0`; estados loading/empty/error/ready |
| `character.service.spec.ts` | bonus | URLs y params omitidos si vacíos |
| `favorite.service.spec.ts` | bonus | set de ids, optimista + reversión |

## Pasos
1. Tabla de casos.
2. `TestBed` con `provideHttpClient()` + `provideHttpClientTesting()`, `provideRouter([])`, dobles de `AuthService`.
3. Sin `setTimeout` reales; `fakeAsync` para el debounce.

## Hecho cuando
`ng test --watch=false` verde (humano).

## Ejecuta y pega
```bash
cd projects/frontend && npx ng test --watch=false 2>&1 | tail -15
```

## Commit propuesto
`test(frontend): cover auth guard, interceptors and character list filtering`
