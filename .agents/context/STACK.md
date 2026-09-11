# Stack objetivo y dependencias previstas

Estado: `EN IMPLEMENTACIÓN`

## Backend

| Elemento | Decisión |
| --- | --- |
| Java | JDK 11, `maven.compiler.release=11` |
| Build | Apache Maven 3.9.11 ejecutado mediante Maven Wrapper |
| Framework | Spring Boot 2.7.18 |
| HTTP | Spring MVC mediante `spring-boot-starter-web` |
| Validación | Bean Validation mediante `spring-boot-starter-validation` |
| Seguridad | Spring Security mediante `spring-boot-starter-security` |
| Relacional | Spring Data JPA + Hibernate 5.6.x gestionado por Spring Boot |
| Migraciones | `liquibase-core`; Liquibase será el único dueño del esquema |
| Driver SQL | PostgreSQL JDBC gestionado por el BOM de Spring Boot |
| Grafo | Spring Data Neo4j + Neo4j Java Driver compatibles con Boot 2.7 |
| Mensajería | Spring Kafka 2.8.x gestionado por Boot 2.7, compatible con broker Kafka 2.0.1 |
| OpenAPI | `springdoc-openapi-ui` 1.8.0, línea compatible con Spring Boot 2 |
| Operación | Spring Boot Actuator |

El futuro `pom.xml` heredará de `spring-boot-starter-parent:2.7.18` para centralizar versiones. Solo se fijarán manualmente versiones no gestionadas o cuando exista una razón de compatibilidad documentada. No se usarán Flyway, WebFlux ni JWT salvo un cambio explícito de decisión.

Dependencias de pruebas previstas:

- `spring-boot-starter-test`.
- `spring-security-test`.
- `spring-kafka-test`.
- Testcontainers para PostgreSQL, Kafka y Neo4j cuando corresponda.
- WireMock o servidor HTTP local equivalente compatible con Java 11 para aislar Rick and Morty API.

## Base de datos y mensajería

| Servicio | Versión objetivo | Uso |
| --- | --- | --- |
| PostgreSQL | 10.23 | Fuente de verdad, usuarios, auditoría, raw payloads y outbox |
| Neo4j Community | 4.4.x fijada en Compose | Proyección del grafo |
| Apache Kafka | 2.0.1, artefacto Scala 2.12 | Transporte de eventos |
| ZooKeeper | Versión compatible con la distribución Kafka fijada | Coordinación del broker legado |

La imagen exacta y su digest se verificarán antes de crear Compose; no se utilizará `latest`.

## Frontend

| Elemento | Decisión |
| --- | --- |
| Framework | Angular 22.1.x |
| Runtime | Node.js 24.19.0 para desarrollo y build; mínimo compatible `^24.15.0` |
| Lenguaje | TypeScript 6.0.x |
| UI | Bootstrap 5.3.8 |
| HTTP | Cliente HTTP oficial de Angular |
| Formularios | Reactive Forms |
| Tests | Runner oficial generado por Angular, pruebas unitarias/de componente/servicio/guard |
| E2E | Fuera de alcance; no instalar Playwright |

Frontend y backend serán proyectos independientes. El frontend obtendrá la URL base desde configuración de entorno y no importará artefactos de construcción del backend.

## Restricciones de compatibilidad

- No subir Java por encima de 11 ni Spring Boot a 3.x/4.x.
- No introducir APIs `jakarta.*` propias de Spring Boot 3; Boot 2.7 usa la generación `javax.*` correspondiente.
- No usar Hibernate 6, Spring Data Neo4j 7/8, Neo4j Driver 6 ni JUnit 6.
- Mantener compatibilidad del cliente Kafka moderno con el broker 2.0.1 y no usar funciones de broker posteriores.
- Toda desviación exige ADR, justificación y autorización del usuario.
