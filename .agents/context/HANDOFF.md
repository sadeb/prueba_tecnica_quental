# Handoff actual

- Actualizado: 2026-09-11
- Fase: `8 - VERIFICATION`
- Estado: `IMPLEMENTACIÓN COMPLETA; CIERRE INTEGRAL PENDIENTE`

## Objetivo vigente

Cerrar la verificación integral de la solución implementada. El orden, los criterios y
las evidencias pendientes están en `NEXT_FEATURES.md`.

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
- Puerto HTTP del backend unificado en `8989` para ejecución local, Compose, healthcheck
  y proxies del frontend.

## Decisiones vigentes

- `AGENTS.md` es la fuente canónica.
- Java 11, Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- Spring MVC, JPA/Hibernate y Liquibase como único gestor del esquema.
- PostgreSQL como fuente de verdad, Neo4j como proyección y Kafka como canal.
- Transactional outbox para consistencia y tokens opacos propios de 256 bits.
- Angular 22.1.x, Node 24.15 o superior compatible, TypeScript 6.0.x, Bootstrap; sin Playwright.
- Los bonus de autenticación, JSON crudo, DLT, mensajes procesados y Liquibase están
  implementados; la cobertura adicional y el reproceso explícito continúan pendientes.

## Verificaciones

- `JAVA_HOME=.../microsoft-11.jdk/Contents/Home ./mvnw -B verify`: build y JAR correctos; 5 pruebas correctas,
  incluida Kafka embebida, migraciones Liquibase, validación Hibernate e idempotencia.
- Informe JaCoCo: 41,5 % de líneas y 30,6 % de ramas; útil como línea base, pero
  insuficiente para considerar cerrado el bonus de cobertura adicional.
- `npm test -- --watch=false`: 4 pruebas Angular correctas en ChromeHeadless.
- `npm run build`: compilación de producción Angular 22 correcta.
- `npm audit` y `npm audit --omit=dev`: 0 vulnerabilidades reportadas.
- `docker compose config --quiet`: configuración válida.
- `docker compose up -d --build backend` y posterior arranque del frontend: los seis
  servicios quedaron saludables; backend con readiness `UP` y frontend con respuesta
  HTTP `200`. Se corrigieron el intervalo del publicador outbox para Spring 5.3 usando
  un valor numérico con `TimeUnit.SECONDS` y el destino IPv4 del healthcheck de Nginx.
- `git diff --check`: correcto, sin errores de whitespace.
- Parseo de `.gemini/settings.json`: correcto.
- Resolución de enlaces Markdown locales: correcta.
- Adaptador `CLAUDE.md`: contiene únicamente `@AGENTS.md`.
- SPA ajustada con estilos mobile-first, shell basado en `100dvh`, contenedores fluidos,
  breakpoints Bootstrap y objetivos táctiles mínimos de 44 px.
- QA responsive de login, registro, catálogo, detalle y página no encontrada en 375x667,
  768x1024, 1024x600 y 1440x900: sin desbordamiento horizontal. Favoritos y
  sincronización redirigieron correctamente a login al no existir sesión.
- Login y registro encajan sin scroll en laptops de 1024x600 y 1366x768; en móvil el
  formulario se prioriza antes del bloque introductorio y el desplazamiento vertical es
  natural.
- Tras los ajustes responsive, `npm test -- --watch=false`: 4 pruebas correctas; y
  `npm run build`: compilación de producción Angular 22 correcta, ambos con Node 24.16.0.
- Contexto persistente de UI creado en `FRONTEND_UI.md` y declarado como lectura
  obligatoria en `AGENTS.md` para cambios de interfaz, layout o estilos.

## Siguiente paso recomendado

Seguir `NEXT_FEATURES.md`, comenzando por P0: pruebas de API/seguridad/favoritos y DLT,
proyección y consulta Neo4j. Después ejecutar el escenario completo desde una base vacía
con `docker compose up --build`.

## Riesgos abiertos

- `.nvmrc` fija Node 24.19.0, pero el ejecutable disponible en esta sesión fue 24.16.0.
  Es compatible con el mínimo `^24.15.0` y se usó para la validación responsive;
  instalar el pin del proyecto al preparar un entorno nuevo.
- Los seis servicios de Compose se validaron sobre los volúmenes locales existentes;
  aún falta repetir el escenario integral desde una base limpia.
- Seguridad, favoritos, DLT, Neo4j, guards e interceptor están implementados, pero aún
  no cuentan con pruebas automatizadas específicas suficientes para cerrar sus puertas.
- OpenAPI genera rutas, parámetros y esquemas, pero todavía no declara de forma
  explícita todos los códigos de error ni aplica el requisito de seguridad a cada operación.
