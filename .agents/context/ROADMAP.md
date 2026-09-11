# Roadmap y puertas de aprobación

## Estado

- Fase activa: `8 - VERIFICATION`.
- Fases autorizadas: 1 a 8 por petición explícita del usuario del 2026-09-11.
- Angular fue creado inicialmente en 20.3.x y ya fue actualizado a Angular 22.1.x.

## Fase 0 - Contexto multiagente

Estado: `IMPLEMENTADO` al completar las verificaciones documentales.

Entregables:

- Fuente canónica `AGENTS.md` y adaptadores mínimos para Claude/Gemini.
- Requisitos trazables, arquitectura, stack, contrato de API y ADR iniciales.
- Carpetas reservadas `backend/`, `frontend/` e `infra/`.
- Skills únicamente sugeridas.

Puerta de salida: estructura válida, enlaces coherentes y ausencia de proyectos, dependencias, código, Compose y skills.

## Fase 1 - Scaffold backend Maven

Estado: `IMPLEMENTADO Y COMPILADO`.

- Crear proyecto Java 11 con Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- Preparar `pom.xml` con MVC, Validation, Security, JPA/Hibernate, PostgreSQL, Liquibase, Neo4j, Kafka, Actuator, OpenAPI y pruebas compatibles.
- Añadir configuración por perfiles y estructura de paquetes por capacidad, sin implementar aún todos los casos de uso.
- Verificar compilación y contexto mínimo.

Puerta de salida: build reproducible en Java 11, árbol de dependencias coherente y ninguna dependencia incompatible con Boot 2.7/Kafka 2.0.1.

## Fase 2 - Scaffold frontend Angular

Estado: `IMPLEMENTADO EN ANGULAR 22; BUILD Y PRUEBAS BASE CORRECTOS`.

- Verificar y fijar Angular estable, Node y TypeScript compatibles en ese momento.
- Generar proyecto independiente con routing, estilos Bootstrap y entorno para la URL de la API.
- Preparar convenciones de componentes, servicios, interceptor, guards y pruebas.
- No instalar Playwright.

Puerta de salida: build y pruebas base reproducibles sin dependencia del backend.

## Fase 3 - Infraestructura local

Estado: `IMPLEMENTADO; CONFIGURACIÓN COMPOSE VALIDADA, ARRANQUE INTEGRAL PENDIENTE`.

- Fijar imágenes compatibles para PostgreSQL 10, Neo4j 4.4, Kafka 2.0.1 y ZooKeeper.
- Crear Dockerfiles de aplicación y Compose completo con health checks, redes, volúmenes y configuración por entorno.
- Proporcionar `.env.example` sin secretos.

Puerta de salida: servicios saludables y comandos documentados.

## Fase 4 - Persistencia y migraciones

Estado: `IMPLEMENTADO; LIQUIBASE E HIBERNATE VALIDADOS EN PRUEBAS`.

- Diseñar modelo relacional, restricciones y claves externas estables.
- Crear changelog maestro y cambios Liquibase incrementales; desactivar generación de esquema por Hibernate.
- Implementar repositorios JPA y proyección Neo4j sin duplicar la fuente de verdad.
- Incorporar usuarios, favoritos, tokens, raw payloads, mensajes procesados, sync runs y outbox.

Puerta de salida: migraciones desde base vacía, validación Hibernate y pruebas de persistencia.

## Fase 5 - Integración, Kafka y Neo4j

Estado: `IMPLEMENTADO; CONSUMO/PERSISTENCIA/IDEMPOTENCIA VALIDADOS, DLT Y NEO4J PENDIENTES DE PRUEBA`.

- Implementar cliente externo validado y aislado de red en pruebas.
- Cerrar ADR de temas, claves, payload versionado, reintentos y DLT.
- Implementar productor, consumidor, deduplicación y transactional outbox.
- Proyectar relaciones de forma idempotente y consultar relacionados en Neo4j.
- Registrar progreso y fallos parciales por ejecución.

Puerta de salida: sincronización repetible y prueba de integración sin infraestructura manual.

## Fase 6 - Seguridad y API

Estado: `IMPLEMENTADO; PRUEBAS ESPECÍFICAS DE AUTORIZACIÓN Y CONTRATO PENDIENTES`.

- Implementar registro, login, logout, tokens opacos y roles.
- Crear bootstrap `ADMIN` mediante entorno de forma segura e idempotente.
- Implementar catálogo, filtros, detalle, relacionados, favoritos y endpoints administrativos.
- Homogeneizar errores y completar OpenAPI.

Puerta de salida: pruebas de autorización, validación, errores y contrato.

## Fase 7 - SPA funcional

Estado: `IMPLEMENTADO; PRUEBAS ESPECÍFICAS DE GUARDS/INTERCEPTOR Y FLUJOS PENDIENTES`.

- Implementar autenticación, catálogo, detalle, relacionados y favoritos.
- Incorporar interceptor, guards, persistencia de sesión y expiración controlada.
- Cubrir estados carga/vacío/error y separar presentación de acceso a datos.
- Añadir pruebas de lógica no trivial y cobertura proporcional.

Puerta de salida: flujo de usuario completo contra la API propia.

## Fase 8 - Verificación y entrega

Estado: `EN CURSO`.

- Ejecutar pruebas backend/frontend e integración completa.
- Validar reinicio, resincronización, DLT, caída temporal de dependencias e idempotencia.
- Completar README operativo, decisiones y troubleshooting.
- Verificar levantamiento limpio mediante Docker Compose y revisar secretos/historial.

Puerta de salida: todos los criterios del PDF reproducibles con instrucciones de entrega.

El trabajo pendiente concreto, su orden y sus criterios de cierre se mantiene en
`NEXT_FEATURES.md` para evitar convertir este roadmap en un listado operativo duplicado.
