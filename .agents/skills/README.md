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
| [angular-spa](angular-spa/SKILL.md) | Cualquier código del frontend (procedimiento del proyecto) |
| [angular-best-practices](angular-best-practices/SKILL.md) | Junto a angular-spa: buenas prácticas oficiales, TypeScript, reglas por versión; índice de las cuatro siguientes |
| [angular-componentes-plantillas](angular-componentes-plantillas/SKILL.md) | Crear o editar componentes, plantillas, accesibilidad |
| [angular-signals-estado](angular-signals-estado/SKILL.md) | `ViewState`, sesión, favoritos, cualquier `signal`/`computed` |
| [angular-formularios](angular-formularios/SKILL.md) | Login, registro, filtros |
| [angular-servicios-di](angular-servicios-di/SKILL.md) | Servicios, `inject()`, interceptores, guardas |
| [testing-aislado](testing-aislado/SKILL.md) | Escribir pruebas backend o frontend |

Las skills no repiten las referencias: enlazan a [references/](../references/README.md) y [conventions/](../conventions/README.md).
