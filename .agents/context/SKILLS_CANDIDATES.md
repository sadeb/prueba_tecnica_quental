# Skills candidatas para fases futuras

Estado: `SOLO SUGERENCIAS - NINGUNA INSTALADA O CREADA`

No existe `.agents/skills/`, ningún `SKILL.md` ni lock de skills en este repositorio. Los comandos siguientes son referencias para ejecución manual futura y no deben ejecutarse sin autorización explícita.

## Candidata externa

### `java-springboot`

- Fuente: [github/awesome-copilot](https://github.com/github/awesome-copilot), repositorio comunitario mantenido por GitHub.
- Uso potencial: convenciones generales de desarrollo Java/Spring Boot.
- Revisión obligatoria previa: confirmar que no imponga Spring Boot 3/4, Java posterior a 11, `jakarta.*`, Hibernate 6 o JUnit 6.
- Instalación manual futura y multiagente, únicamente si la revisión es satisfactoria:

```bash
npx skills add https://github.com/github/awesome-copilot --skill java-springboot --agent '*' -y
```

## Skills locales propuestas

Cuando exista código real podrán diseñarse bajo `.agents/skills/<name>/SKILL.md`, siguiendo el formato abierto de Agent Skills:

| Skill | Finalidad |
| --- | --- |
| `quental-requirements` | Consultar trazabilidad y criterios de aceptación sin releer todo el PDF |
| `quental-database-migrations` | Convenciones Liquibase, PostgreSQL 10, rollback y validación de esquema |
| `quental-backend` | Arquitectura Spring MVC/JPA/Kafka/Neo4j específica del repositorio |
| `quental-frontend` | Convenciones Angular, servicios, interceptor, guards, estado y pruebas |
| `quental-release-check` | Verificación repetible de build, tests, Compose, secretos y documentación |

Crear estas skills antes de tener los proyectos produciría reglas especulativas. Cada una deberá limitar su alcance, incluir criterios de activación claros y apuntar al contexto canónico en vez de duplicarlo.

## Candidatas que deben evitarse

- Skills exclusivas para Spring Boot 3.x/4.x o Java 17/21.
- Skills basadas en Hibernate 6, Spring Data Neo4j 7/8 o Neo4j Driver 6.
- Skills que requieran JUnit 6.
- Skills archivadas o sin procedencia verificable para Angular.
- Skills que instalen herramientas automáticamente o reemplacen decisiones aceptadas del proyecto.

## Proceso manual recomendado

1. Autorizar la fase de proyecto correspondiente.
2. Revisar fuente, licencia, actividad y contenido completo de la skill.
3. Compararla con `STACK.md`, `AGENTS.md` y los ADR.
4. Probarla en una rama aislada y revisar todos los archivos creados.
5. Instalarla solo con confirmación del usuario y registrar versión/origen.
