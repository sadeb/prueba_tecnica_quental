# references/ — Referencias técnicas

Notas breves por tecnología, **en las versiones fijadas**. Hechos verificables, no opiniones; las decisiones van en [decisions/](../decisions/README.md).

| Fichero | Tema |
|---|---|
| [stack-versiones.md](stack-versiones.md) | Versiones concretas de cada pieza y compatibilidades |
| [rick-and-morty-api.md](rick-and-morty-api.md) | Contrato real de la fuente externa e inconsistencias conocidas |
| [spring-boot-2.7.md](spring-boot-2.7.md) | Spring Boot 2.7 / MVC / Data en JDK 11 |
| [kafka-2.0.1.md](kafka-2.0.1.md) | Broker 2.0.1, spring-kafka, imágenes Docker |
| [postgresql-10.md](postgresql-10.md) | Postgres 10, JPA, migraciones |
| [neo4j.md](neo4j.md) | Neo4j 4.4, Spring Data Neo4j 6, Cypher para el grafo |
| [angular-bootstrap.md](angular-bootstrap.md) | Angular CLI, Bootstrap, interceptores, guardas |
| [docker-compose.md](docker-compose.md) | Servicios, imágenes, healthchecks, redes |
| [openapi-springdoc.md](openapi-springdoc.md) | Documentación OpenAPI en Boot 2.7 |
| [testing-backend.md](testing-backend.md) | JUnit 5, MockMvc, EmbeddedKafka, H2/Testcontainers |
| [testing-frontend.md](testing-frontend.md) | Pruebas de servicios, guardas y componentes en Angular |

Regla: si una versión aquí resulta incompatible al construir, se corrige este fichero y se anota en el ADR correspondiente.
