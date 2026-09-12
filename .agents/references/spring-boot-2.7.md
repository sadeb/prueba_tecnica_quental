# Spring Boot 2.7 en JDK 11

- Namespace **`javax.*`** (persistence, validation, servlet). Nunca `jakarta.*` (eso es Boot 3).
- Starters a usar: `web`, `validation`, `data-jpa`, `data-neo4j`, `kafka`, `security`, `actuator` (opcional, solo `/actuator/health`), `test`, `kafka-test`.
- Cliente HTTP: **`RestTemplate`** vía `RestTemplateBuilder` con `setConnectTimeout`/`setReadTimeout`. No añadir WebFlux solo por `WebClient`.
- Configuración tipada: `@ConfigurationProperties` + `@Validated` (p. ej. `external-api.base-url`, `timeouts`, `sync.topics`).
- Perfiles: `default` (docker-compose), `test`. Variables de entorno sobreescriben `application.yml`.
- Manejo de errores: `@RestControllerAdvice` + `ResponseEntity<ApiError>` ([conventions/formato-error.md](../conventions/formato-error.md)).
- Paginación: `Pageable`/`Page` de Spring Data en JPA; en la API exponer `page`, `size`, `totalElements`, `totalPages`, `content`.
- Transacciones: `@Transactional` solo en servicios de aplicación; Neo4j y JPA tienen gestores distintos → no hay transacción distribuida ([ADR-004](../decisions/ADR-004-consistencia-postgres-neo4j.md)).
- Arranque de la sync: `ApplicationRunner` condicionado por propiedad, o endpoint `POST /api/admin/sync`. Ver [workflows/04-productor-kafka.md](../workflows/04-productor-kafka.md).
- Salud de dependencias en compose: Boot reintenta conexión a Postgres/Neo4j al arrancar solo si el contenedor ya responde; usar `depends_on` + `healthcheck` ([docker-compose.md](docker-compose.md)).

Convenciones de código: [conventions/java-spring.md](../conventions/java-spring.md).
