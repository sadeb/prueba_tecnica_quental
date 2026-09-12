# PostgreSQL 10

- Imagen `postgres:10.23`. Variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`. Healthcheck: `pg_isready -U $POSTGRES_USER`.
- Driver JDBC 42.x (gestionado por Boot) es compatible con 10.
- Tipos útiles disponibles en 10: `jsonb` (bonus JSON crudo), `bigserial`, `timestamptz`, índices únicos parciales, `ON CONFLICT ... DO UPDATE` (upsert nativo, desde 9.5).
- Hibernate dialect: `org.hibernate.dialect.PostgreSQL10Dialect`.
- `spring.jpa.hibernate.ddl-auto=validate` en producción del compose; el esquema lo crea Flyway (`V1__init.sql`, `V2__...`). En tests con H2 puede usarse `create-drop` o Flyway con H2 en modo Postgres (`MODE=PostgreSQL`).
- Convención de nombres: tablas y columnas en `snake_case`, plural para tablas (`characters`, `episodes`, `locations`, `character_episodes`, `users`, `user_favorites`, `raw_payloads`, `sync_runs`, `processed_messages`).
- Upsert idempotente por `external_id` (constraint `UNIQUE`). Ver [ADR-001](../decisions/ADR-001-identificador-externo.md) y [ADR-002](../decisions/ADR-002-idempotencia-sync.md).
- Tabla N:M `character_episodes(character_id, episode_id)` con PK compuesta → reinsertar no duplica.

Relacionado: [spec/04-modelo-de-datos.md](../spec/04-modelo-de-datos.md), [workflows/06-persistencia-postgres.md](../workflows/06-persistencia-postgres.md).
