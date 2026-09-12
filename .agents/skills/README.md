# skills/ — Procedimientos y restricciones

Formato estándar Agent Skills: una carpeta por skill con `SKILL.md` (frontmatter `name`, `description`) y, si hace falta, ficheros auxiliares. Todos los agentes las descubren y leen desde esta carpeta (`.agents/skills/`), sin copias ni enlaces en carpetas propietarias.

## Obligatorias (siempre activas)
| Skill | Propósito |
|---|---|
| [no-git-write](no-git-write/SKILL.md) | Prohíbe commits, push y reescritura de historial |
| [no-run-commands](no-run-commands/SKILL.md) | Prohíbe ejecutar backend, frontend, docker compose y tests |
| [economia-tokens](economia-tokens/SKILL.md) | Minimiza contexto cargado y salida generada |

## Técnicas (cargar según el paso)
| Skill | Cuándo |
|---|---|
| [spring-boot-jdk11](spring-boot-jdk11/SKILL.md) | Cualquier código Java del backend |
| [kafka-sync](kafka-sync/SKILL.md) | Productor, consumidor, topics, DLT |
| [neo4j-graph](neo4j-graph/SKILL.md) | Nodos, Cypher, consulta de relacionados |
| [angular-spa](angular-spa/SKILL.md) | **Entrada única del frontend**: procedimiento, TypeScript, reglas por versión, lista de prohibido e índice de las cuatro hojas siguientes |
| [angular-componentes-plantillas](angular-componentes-plantillas/SKILL.md) | Hoja: `.component.ts`, `.page.ts`, plantillas, accesibilidad |
| [angular-signals-estado](angular-signals-estado/SKILL.md) | Hoja: `ViewState`, sesión, favoritos, cualquier `signal` / `computed` |
| [angular-formularios](angular-formularios/SKILL.md) | Hoja: login, registro, filtros |
| [angular-servicios-di](angular-servicios-di/SKILL.md) | Hoja: `.service.ts`, `inject()`, interceptores, guardas, `app.config.ts` |
| [testing-aislado](testing-aislado/SKILL.md) | Escribir pruebas backend o frontend |

Frontend: se carga `angular-spa` más **solo** la hoja del fichero que se toca; las hojas no se enlazan entre sí. La documentación oficial de Angular filtrada vive en [references/angular-oficial/](../references/angular-oficial/README.md) y no es una skill.

Las skills no repiten las referencias: enlazan a [references/](../references/README.md) y [conventions/](../conventions/README.md).
