# angular-oficial/ — Documentación oficial de Angular (extracto filtrado)

Copia de 13 de las 43 references del skill `angular-developer` publicado por el equipo de Angular (2026, licencia MIT). **No es una skill**: no tiene `SKILL.md` y ningún agente la descubre como tal. Se lee un fichero concreto solo cuando una hoja de [skills/angular-spa](../../skills/angular-spa/SKILL.md) lo enlaza y sus reglas no bastan. En caso de conflicto mandan las reglas del proyecto ([ADR-007](../../decisions/ADR-007-best-practices-angular.md)); en particular, aquí se ordena ejecutar `ng build` o `ng generate` y en este repositorio lo hace el humano ([no-run-commands](../../skills/no-run-commands/SKILL.md)).

| Fichero | Tema | Lo enlaza |
|---|---|---|
| [components.md](components.md) | Anatomía del componente, metadatos, control flow `@if` / `@for` / `@switch` | angular-componentes-plantillas |
| [inputs.md](inputs.md) | `input()`, transformaciones, `model()` | angular-componentes-plantillas |
| [outputs.md](outputs.md) | `output()` y eventos personalizados | (complemento de inputs.md) |
| [host-elements.md](host-elements.md) | Propiedad `host`, atributos del host | (complemento de components.md) |
| [signals-overview.md](signals-overview.md) | `signal`, `computed`, contextos reactivos, `untracked` | angular-signals-estado |
| [linked-signal.md](linked-signal.md) | `linkedSignal()` | angular-signals-estado |
| [effects.md](effects.md) | `effect()` y cuándo **no** usarlo | (complemento de signals-overview.md) |
| [creating-services.md](creating-services.md) | Servicios, `@Service`, `providedIn: 'root'`, inyección | angular-servicios-di |
| [http-client.md](http-client.md) | `provideHttpClient`, `HttpClient`, interceptores funcionales | angular-servicios-di |
| [reactive-forms.md](reactive-forms.md) | Formularios reactivos tipados (Angular < 22) | angular-formularios |
| [signal-forms.md](signal-forms.md) | Signal Forms (Angular ≥ 22); 900 líneas, leer solo la sección necesaria | angular-formularios |
| [testing-fundamentals.md](testing-fundamentals.md) | `TestBed`, pruebas asíncronas, Vitest | references/testing-frontend.md |
| [router-testing.md](router-testing.md) | `RouterTestingHarness` para guardas y navegación | references/testing-frontend.md |

Descartado por no aplicar al proyecto (Bootstrap, sin SSR, sin Tailwind, sin CLI ejecutada por el agente): Angular Aria, Tailwind, SSR y estrategias de render, animaciones, MCP, CLI, migraciones, E2E, nomenclatura v20+ (choca con `.page.ts` de [conventions/angular.md](../../conventions/angular.md)), rutas, resolvers, pipes, proveedores y jerarquía de inyectores, estilos de componente y harnesses. Si alguno hiciera falta, se recupera del commit `bfbea6a`.
