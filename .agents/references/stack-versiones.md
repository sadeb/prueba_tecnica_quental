# Versiones concretas del stack

Requisito origen: [spec/01-entorno-y-versiones.md](../spec/01-entorno-y-versiones.md). Verificar cada versión en Maven Central / Docker Hub antes de fijarla en el POM o en `docker-compose.yml`.

## Backend (JDK 11)
| Pieza | Versión propuesta | Nota |
|---|---|---|
| JDK | 11 (Temurin) | `maven.compiler.release=11`. Imagen build: `eclipse-temurin:11-jdk`; runtime: `eclipse-temurin:11-jre` |
| Spring Boot | 2.7.18 | Última 2.7.x. Gestiona Spring Kafka 2.8.x, Spring Data JPA 2.7, SDN 6.3, Flyway 8.5 |
| Build | Maven 3.9 (wrapper `mvnw`) | Sin Gradle: menos superficie |
| spring-kafka | 2.8.11 (gestionada) | kafka-clients 3.1.x; compatible con broker 2.0.1 (ver [kafka-2.0.1.md](kafka-2.0.1.md)) |
| Spring Data JPA + Hibernate | gestionadas (Hibernate 5.6) | Driver `org.postgresql:postgresql` gestionado |
| Spring Data Neo4j | 6.3.x (gestionada) | Driver Java 4.4; servidor Neo4j 4.4 |
| Flyway | 8.5.x (gestionada) | Bonus migraciones ([spec/08-bonus.md](../spec/08-bonus.md) B4) |
| Spring Security | 5.7.x (gestionada) | Solo filtro + `PasswordEncoder`; token propio ([ADR-005](../decisions/ADR-005-autenticacion.md)) |
| springdoc-openapi-ui | 1.7.0 | Última rama 1.x, compatible con Boot 2.x / JDK 11 |
| Validación | `spring-boot-starter-validation` (Hibernate Validator 6, javax) | Boot 2.7 usa `javax.*`, no `jakarta.*` |

## Infraestructura (docker-compose)
| Servicio | Imagen | Nota |
|---|---|---|
| PostgreSQL | `postgres:10.23` | Última 10.x publicada |
| Neo4j | `neo4j:4.4` | LTS compatible con SDN 6 / driver 4.4 |
| Zookeeper | `confluentinc/cp-zookeeper:5.0.1` o `zookeeper:3.4` | Kafka 2.0.1 requiere ZK |
| Kafka | `confluentinc/cp-kafka:5.0.1` (= Kafka 2.0.1) o `wurstmeister/kafka:2.12-2.0.1` | Ver [kafka-2.0.1.md](kafka-2.0.1.md) |

## Frontend
| Pieza | Versión | Nota |
|---|---|---|
| Node | LTS vigente (20 o 22) | Fijar en `.nvmrc` y `engines` |
| Angular | Última LTS del CLI en el momento de crear el proyecto | Standalone components; anotar versión exacta en [angular-bootstrap.md](angular-bootstrap.md) tras `ng new` |
| Bootstrap | 5.3.x (CSS vía npm) | Sin ng-bootstrap por economía de dependencias |

Dependencias **no** incluidas por defecto (requieren ADR): resilience4j, spring-retry, lombok, mapstruct, jjwt, ng-bootstrap, ngrx, testcontainers (opcional en tests).
