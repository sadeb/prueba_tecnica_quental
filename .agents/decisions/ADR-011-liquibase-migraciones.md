# ADR-011 · Liquibase como gestor de migraciones de PostgreSQL

**Estado**: Propuesta · **Requisito**: bonus B4 ([spec/08](../spec/08-bonus.md)); [spec/04](../spec/04-modelo-de-datos.md) · **Fecha**: 2026-09-13

## Contexto
El esquema relacional debe estar versionado y aplicarse solo al arrancar el backend (`ddl-auto=validate`). Hasta ahora lo gestionaba Flyway (`V1__init.sql` … `V4__user_favorites.sql`). Se fija Liquibase como gestor de migraciones por decisión del proyecto; la migración debe seguir funcionando en PostgreSQL 10 y en H2 2.x `MODE=PostgreSQL` (tests sin Docker).

## Decisión
- Sustituir `flyway-core` por `liquibase-core` (versión gestionada por el BOM de Boot 2.7.18: 4.9.x). Cero configuración extra: `spring.liquibase.change-log` apunta a `classpath:db/changelog/db.changelog-master.yaml`.
- **Changelog maestro en YAML** que incluye, en orden, un fichero **formatted SQL** por cambio (`db/changelog/changes/NNN-nombre.sql`, `--changeset rickmorty:NNN-nombre`). El DDL ya revisado para el subconjunto Postgres 10 / H2 se conserva literal; no se reescribe en el DSL XML/YAML de Liquibase.
- Numeración `001`, `002`… en el nombre y en el id del changeset; un changeset por fichero; nunca editar un changeset aplicado (Liquibase valida el checksum y aborta el arranque si cambia).
- Liquibase corre antes de que Hibernate valide el esquema (Boot ordena `EntityManagerFactory` tras `SpringLiquibase`) y antes de los `ApplicationRunner` (entre ellos el del administrador, [ADR-012](ADR-012-administrador-sistema.md)).
- El volumen `pg_data` creado con Flyway (`flyway_schema_history`) no se migra: es entorno de desarrollo y `docker compose down -v` lo deja limpio. Se documenta en `.env` y README.

## Alternativas descartadas
- Mantener Flyway — equivalente técnicamente, pero el proyecto fija Liquibase como gestor.
- Changesets en XML/YAML nativo — portables entre motores, pero obligan a reescribir y re-verificar el DDL en H2 y Postgres; el formatted SQL da el mismo control de versiones (`databasechangelog`, checksums, bloqueo) con el SQL ya validado.
- `includeAll` sobre el directorio — orden por nombre de fichero implícito; el `include` explícito hace visible el orden en el maestro.
- Precondiciones `MARK_RAN` para adoptar una base creada por Flyway — complejidad que no aporta nada en un entorno que se recrea con `down -v`.

## Consecuencias
Misma garantía que antes (esquema versionado, validado por Hibernate) con la herramienta fijada por el proyecto. Tablas de control `databasechangelog` / `databasechangeloglock`. Vigilar: un arranque abortado a mitad deja el lock cogido (`databasechangeloglock.locked=true`); se libera con `update databasechangeloglock set locked=false` o `down -v`. En entrevista: "el SQL es el mismo; solo cambia quién lleva el libro de versiones".

## Dónde se aplica
`projects/backend/pom.xml`, `src/main/resources/db/changelog/`, `application.yml` / `application-test.yml`; [wf 06](../workflows/06-persistencia-postgres.md).
