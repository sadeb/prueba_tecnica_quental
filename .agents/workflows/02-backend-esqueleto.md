# 02 · Esqueleto backend

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [spring-boot-jdk11](../skills/spring-boot-jdk11/SKILL.md)

## Objetivo
Proyecto Maven Spring Boot 2.7.18 sobre JDK 11 que compila, con estructura de paquetes, configuración base, manejo de errores global y Dockerfile.

## Contexto
[conventions/java-spring.md](../conventions/java-spring.md), [conventions/formato-error.md](../conventions/formato-error.md), [references/spring-boot-2.7.md](../references/spring-boot-2.7.md), [references/stack-versiones.md](../references/stack-versiones.md).

## Pasos
1. `projects/backend/pom.xml`: parent `spring-boot-starter-parent` 2.7.18, `java.version` 11, starters `web`, `validation`, `data-jpa`, `data-neo4j`, `kafka`, `security`, `actuator`, `test`, `kafka-test`, driver `postgresql`, `liquibase-core` ([ADR-011](../decisions/ADR-011-liquibase-migraciones.md)), `springdoc-openapi-ui` 1.7.0, `h2` (test). Maven wrapper.
2. Paquetes de [java-spring.md](../conventions/java-spring.md) con `package-info.java` o clase vacía donde aún no haya código (evitar carpetas vacías).
3. `application.yml`: datasource, neo4j, kafka, `sync.*`, `external-api.*`, `auth.token.*`, `auth.admin.*` ([ADR-012](../decisions/ADR-012-administrador-sistema.md)) leyendo variables de entorno con valores por defecto para `localhost`. `application-test.yml` con H2 y Kafka embebido.
4. `common/`: `ApiError`, excepciones base, `GlobalExceptionHandler`, `PageResponse<T>`.
5. `config/SecurityConfig` provisional que permita todo (se endurece en [08](08-autenticacion.md)) y desactive CSRF; `/actuator/health` expuesto.
6. `projects/backend/Dockerfile` ya existe ([01](01-infraestructura-docker.md)): espera `pom.xml` y `src/` en `projects/backend/` y extrae el jar en capas. No modificarlo salvo que cambie el layout; el `pom.xml` debe llevar `spring-boot-maven-plugin` (jar ejecutable en capas, por defecto en Boot 2.7).
7. `application.yml` debe leer las variables del servicio `backend` del compose (`SPRING_*` por relaxed binding, `SYNC_ON_STARTUP`, `AUTH_TOKEN_SECRET`, `AUTH_TOKEN_TTL_HOURS`) y exponer `/actuator/health` sin autenticación (healthcheck).

## Hecho cuando
Compila; arranca contra la infraestructura del compose y `/actuator/health` responde `UP` (humano).

## Ejecuta y pega
```bash
cd projects/backend && ./mvnw -q -DskipTests package && echo BUILD_OK
```
Pegar solo líneas `[ERROR]` si las hay.

## Commit propuesto
`chore(backend): scaffold spring boot 2.7 project on jdk 11 with base config and error handling`
