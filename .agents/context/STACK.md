# Stack objetivo y dependencias previstas

Estado: `PLANIFICADO - NO INSTALADO`

Las versiones aquí registradas son decisiones para las futuras fases. No existe todavía `pom.xml`, proyecto Angular, wrapper ni descarga de dependencias.

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
| Framework | Angular estable vigente al autorizar la fase frontend; se fijará la versión exacta antes del scaffold |
| Lenguaje | TypeScript compatible con la versión Angular elegida |
| UI | Bootstrap 5.x fijado |
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
