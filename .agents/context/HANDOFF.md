# Handoff actual

- Actualizado: 2026-09-11
- Fase: `8 - VERIFICATION`
- Estado: `IMPLEMENTACIÓN COMPLETA; CIERRE INTEGRAL PENDIENTE`

## Objetivo vigente

Implementar la solución completa sobre el estado actual: actualizar el scaffold a Angular 22, crear el backend Spring Boot 2.7.18 con Maven, definir DTOs a partir de la API real, usar Liquibase, completar seguridad, sincronización, persistencia políglota, pruebas e infraestructura.

## Hecho en esta fase

- PDF de requisitos leído y revisado visualmente en sus tres páginas.
- Requisitos clasificados por procedencia.
- Contexto neutral para agentes y adaptadores mínimos preparados.
- Arquitectura objetivo y cuatro decisiones iniciales documentadas.
- Roadmap protegido mediante puertas de aprobación.
- Directorios de aplicación reservados sin scaffolds.
- El usuario instaló las skills Angular, frontend y Spring Boot.
- El usuario generó un scaffold Angular que la inspección identifica como 20.3.31.
- La API REST real y su documentación fueron inspeccionadas para fijar los DTO externos.
- Backend Java 11/Spring Boot 2.7.18 creado con Maven Wrapper 3.9.11, MVC, Security,
  JPA/Hibernate, Liquibase, Kafka, Neo4j, outbox y OpenAPI.
- SPA actualizada a Angular 22 con Bootstrap, rutas lazy, guards, interceptor, Signal
  Forms y pantallas de autenticación, catálogo, detalle, favoritos y sincronización.
- Compose, Dockerfiles, Nginx y configuración de entorno creados.

## Decisiones vigentes

- `AGENTS.md` es la fuente canónica.
- Java 11, Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- Spring MVC, JPA/Hibernate y Liquibase como único gestor del esquema.
- PostgreSQL como fuente de verdad, Neo4j como proyección y Kafka como canal.
- Transactional outbox para consistencia y tokens opacos propios de 256 bits.
- Angular 22.1.x, Node 24.15 o superior compatible, TypeScript 6.0.x, Bootstrap; sin Playwright.
- Todos los bonus del PDF están incluidos en el alcance futuro.

## Verificaciones

- `git diff --check`: correcto, sin errores de whitespace.
- Parseo de `.gemini/settings.json`: correcto.
- Resolución de enlaces Markdown locales: correcta.
- Adaptador `CLAUDE.md`: contiene únicamente `@AGENTS.md`.
- `JAVA_HOME=.../microsoft-11.jdk/Contents/Home ./mvnw -B verify`: build y JAR correctos; 5 pruebas correctas,
  incluida Kafka embebida, migraciones Liquibase, validación Hibernate e idempotencia.
- Informe JaCoCo: 41,5 % de líneas y 30,6 % de ramas; útil como línea base, pero
  insuficiente para considerar cerrado el bonus de cobertura adicional.
- `npm test -- --watch=false`: 4 pruebas Angular correctas en ChromeHeadless.
- `npm run build`: compilación de producción Angular 22 correcta.
- `npm audit` y `npm audit --omit=dev`: 0 vulnerabilidades reportadas.
- `docker compose config --quiet`: configuración válida.
- `git diff --check`: correcto, sin errores de whitespace.
- Parseo de `.gemini/settings.json`: correcto.
- QA visual manual del acceso en viewport estrecho: correcto.

## Siguiente paso recomendado

Completar las evidencias de cierre: pruebas de API/seguridad/favoritos, guards e
interceptor frontend, DLT y proyección/consulta Neo4j; después ejecutar el escenario
completo desde una base vacía con `docker compose up --build`.

## Riesgos abiertos

- El Node local 22.12.0 no soporta Angular 22; las validaciones se ejecutaron con Node
  24.19.0 y ese mínimo está fijado en el proyecto.
- No se ejecutó todavía el levantamiento integral de los seis servicios de Compose;
  únicamente se validó la configuración.
- Seguridad, favoritos, DLT, Neo4j, guards e interceptor están implementados, pero aún
  no cuentan con pruebas automatizadas específicas suficientes para cerrar sus puertas.
- OpenAPI genera rutas, parámetros y esquemas, pero todavía no declara de forma
  explícita todos los códigos de error ni aplica el requisito de seguridad a cada operación.
