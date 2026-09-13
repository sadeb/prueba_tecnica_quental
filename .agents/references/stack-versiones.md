# Versiones concretas del stack

Requisito origen: [spec/01-entorno-y-versiones.md](../spec/01-entorno-y-versiones.md). Verificar cada versión en Maven Central / Docker Hub antes de fijarla en el POM o en `docker-compose.yml`. Imágenes verificadas en Docker Hub el 2026-09-13 ([ADR-008](../decisions/ADR-008-imagenes-docker.md)).

## Backend (JDK 11)
| Pieza | Versión propuesta | Nota |
|---|---|---|
| JDK | 11 (Temurin) | `maven.compiler.release=11`. Imagen build: `maven:3.9.16-eclipse-temurin-11`; runtime: `eclipse-temurin:11.0.31_11-jre-jammy` (`11-jre-alpine` es solo amd64: descartada) |
| Spring Boot | 2.7.18 | Última 2.7.x. Gestiona Spring Kafka 2.8.x, Spring Data JPA 2.7, SDN 6.3, Flyway 8.5 |
| Build | Maven 3.9 (wrapper `mvnw` para el humano; la imagen Docker usa `maven:3.9.16`) | Sin Gradle: menos superficie |
| spring-kafka | 2.8.11 (gestionada) | Artefacto `org.springframework.kafka:spring-kafka` (no existe starter). kafka-clients 3.1.x; compatible con broker 2.0.1 (ver [kafka-2.0.1.md](kafka-2.0.1.md)) |
| Spring Data JPA + Hibernate | gestionadas (Hibernate 5.6) | Driver `org.postgresql:postgresql` gestionado |
| Spring Data Neo4j | 6.3.x (gestionada) | Driver Java 4.4; servidor Neo4j 4.4 |
| Flyway | 8.5.x (gestionada) | Bonus migraciones ([spec/08-bonus.md](../spec/08-bonus.md) B4) |
| Spring Security | 5.7.x (gestionada) | Solo filtro + `PasswordEncoder`; token propio ([ADR-005](../decisions/ADR-005-autenticacion.md)) |
| springdoc-openapi-ui | 1.8.0 | Última release de la rama 1.x (oct-2024), compatible con Boot 2.x / JDK 11. Activable por `SWAGGER_ENABLED` ([ADR-009](../decisions/ADR-009-openapi-toggle.md)) |
| Validación | `spring-boot-starter-validation` (Hibernate Validator 6, javax) | Boot 2.7 usa `javax.*`, no `jakarta.*` |

## Infraestructura (docker-compose)
Todas multi-arch (amd64 + arm64). Tamaño comprimido arm64 entre paréntesis.

| Servicio | Imagen | Nota |
|---|---|---|
| PostgreSQL | `postgres:10.23-alpine` (29 MB) | 10.23 = última release de la rama 10 (EOL nov-2022) |
| Neo4j | `neo4j:4.4.48-community` (313 MB) | 4.4 LTS compatible con SDN 6 / driver 4.4; trae `wget` |
| Zookeeper | `zookeeper:3.4.13` (92 MB) | Versión de ZK que empaqueta Kafka 2.0.1; imagen oficial. `3.4.14` y `confluentinc/cp-zookeeper:5.0.1` son solo amd64 |
| Kafka | `wurstmeister/kafka:2.12-2.0.1` (185 MB) | Tag idéntico al enunciado. `confluentinc/cp-kafka:5.0.1` (= Kafka 2.0.1) es solo amd64 y de 2018; `bitnami/kafka:2.0.1` retirada |
| Backend build | `maven:3.9.16-eclipse-temurin-11` (210 MB, solo stage build) | Maven + Temurin 11 oficial |
| Backend runtime | `eclipse-temurin:11.0.31_11-jre-jammy` (85 MB) | JRE 11 más ligera disponible en arm64; incluye `curl` para el healthcheck |
| Frontend build | `node:24.21.0-alpine` (59 MB, solo stage build) | Ver Frontend |
| Frontend runtime | `nginx:1.30.4-alpine-slim` (9 MB) | nginx stable sin módulos extra ni perl |

## Frontend
| Pieza | Versión | Nota |
|---|---|---|
| Node | 24 LTS (`node:24.21.0-alpine`) | Angular 22 exige Node `^22.22.3 \|\| ^24.15.0`; Node 20 está fuera de soporte desde abril 2026. Fijar en `.nvmrc` y `engines` |
| Angular | Última estable del CLI en el momento de crear el proyecto (22.x en sept-2026) | Standalone components; anotar versión exacta en [angular-bootstrap.md](angular-bootstrap.md) tras `ng new` |
| Bootstrap | 5.3.x (CSS vía npm) | Sin ng-bootstrap por economía de dependencias |

Dependencias **no** incluidas por defecto (requieren ADR): resilience4j, spring-retry, lombok, mapstruct, jjwt, ng-bootstrap, ngrx, testcontainers (opcional en tests).
