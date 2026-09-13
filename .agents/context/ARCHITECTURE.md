# Arquitectura implementada

Estado: `IMPLEMENTADA; VALIDACIÓN INTEGRAL PARCIAL`

Este documento describe la arquitectura autorizada e implementada. La verificación
integral pendiente está priorizada en `NEXT_FEATURES.md`.

## Principios

- PostgreSQL es la fuente de verdad para atributos sincronizados y toda la funcionalidad de usuario.
- Neo4j es una proyección reconstruible de relaciones, no un segundo origen de verdad.
- Kafka desacopla la descarga externa del procesamiento y la persistencia.
- Los límites externos se traducen a modelos propios antes de entrar en el dominio.
- Las operaciones repetidas deben converger al mismo estado.
- Cada fallo parcial debe ser observable y recuperable sin volver a descargar cuando sea posible.
- El frontend solo conoce el contrato HTTP de la API propia.

## Componentes implementados

```text
Angular SPA
    |
    v
Spring MVC API ----> PostgreSQL (source of truth)
    |                       |
    |                       v
    |                Transactional outbox
    |                       |
    v                       v
Neo4j query layer <---- Kafka consumers <---- Kafka
                                                ^
                                                |
                                  Sync producer / external client
                                                |
                                                v
                                      Rick and Morty API
```

## Límites del backend

Se adoptará una organización por capacidad con separación interna de responsabilidades:

- `auth`: alta administrativa de usuarios, login, tokens opacos, roles y bootstrap administrativo.
- `catalog`: personajes, episodios, localizaciones, filtros y detalle.
- `favorites`: favoritos del usuario autenticado.
- `sync`: coordinación, cliente externo, payload crudo, productor, consumidor e idempotencia.
- `graph`: proyección Neo4j y consultas de relaciones.
- `outbox`: publicación fiable de eventos pendientes.
- `shared`: errores HTTP, configuración, observabilidad y utilidades transversales estrictamente necesarias.

Los controladores dependerán de casos de uso, no de repositorios ni clientes externos. Los DTO de Rick and Morty, los modelos HTTP propios, las entidades JPA y los nodos Neo4j serán tipos diferentes.

## Flujo de sincronización implementado

1. Un administrador inicia una ejecución mediante la API; opcionalmente, una propiedad
   puede activarla al arrancar. Ambos disparadores se registran en `sync_runs` como
   `MANUAL` o `AUTOMATIC`.
2. El coordinador registra una `sync_run`, pagina Rick and Morty API y aplica timeouts y validación.
3. Cada respuesta válida se conserva como payload crudo con identidad reproducible.
4. El productor publica mensajes con versión de esquema y una clave estable basada en tipo e identificador externo.
5. El consumidor verifica el identificador del mensaje procesado y aplica upsert transaccional en PostgreSQL.
6. En la misma transacción se escriben eventos outbox para actualizar la proyección del grafo.
7. El publicador outbox envía los eventos a Kafka; el consumidor de grafo hace `MERGE` idempotente en Neo4j.
8. Los errores reintentables usan backoff acotado. Los no recuperables terminan en DLT y quedan ligados a la ejecución.
9. La ejecución expone conteos de páginas, mensajes, éxitos y errores; los fallos parciales no se ocultan.

La definición exacta de temas, payloads y reintentos está registrada en ADR-0006; las tablas se gestionan exclusivamente mediante Liquibase.

## Modelo conceptual

- Identidad sincronizada: clave única compuesta por `source` y `externalId` en PostgreSQL y Neo4j.
- `Character`: atributos del proveedor, origen, ubicación actual y episodios.
- `Episode`: atributos y personajes participantes.
- `Location`: atributos y residentes derivados de la ubicación actual.
- `User`: credenciales, rol y favoritos, solo en PostgreSQL.
- `AccessToken`: hash, propietario, expiración, revocación y metadatos mínimos, solo en PostgreSQL.
- `RawPayload`, `ProcessedMessage`, `OutboxEvent` y `SyncRun`: trazabilidad y recuperación, solo en PostgreSQL.

Neo4j contendrá nodos con identidad y los atributos mínimos necesarios para consultar, evitando convertirlo en origen de atributos. Relaciones previstas: `APPEARED_IN`, `ORIGINATED_FROM` y `CURRENTLY_LOCATED_AT`.

## Consistencia y fallos

- La transacción PostgreSQL confirma conjuntamente el cambio de dominio y su evento outbox.
- Publicar varias veces el mismo evento no altera el resultado: consumidores deduplican y usan upsert/`MERGE`.
- Una indisponibilidad de Neo4j no revierte datos ya válidos en PostgreSQL; la proyección queda reintentable.
- La API de detalle puede combinar PostgreSQL y Neo4j. Si el grafo no está disponible, responderá un error homogéneo y observable para la parte que requiere relaciones; no inventará datos.
- Las credenciales y los tokens nunca se publican en Kafka ni se almacenan en Neo4j.

## Seguridad

- Contraseñas mediante el codificador robusto proporcionado por Spring Security.
- Token opaco generado con CSPRNG y al menos 256 bits de entropía.
- El token plano solo se devuelve al emitirlo; PostgreSQL guarda un hash criptográfico.
- Cada petición autenticada valida hash, expiración y revocación.
- Endpoints administrativos restringidos a `ADMIN`; credenciales iniciales solo mediante variables de entorno.
- No existe autorregistro: solo un administrador autenticado puede crear cuentas, que
  nacen habilitadas con rol `USER`.

## Frontend

- Aplicación independiente, con su propio ciclo de construcción y despliegue.
- Servicios para API y estado de sesión; interceptor para autorización y normalización de fallos.
- Guards para rutas privadas y administrativas.
- Componentes de presentación sin llamadas HTTP directas.
- La pantalla administrativa usa signals para su estado local y consulta cada minuto la
  ejecución manual recién iniciada hasta alcanzar un estado terminal.
- Modelado explícito de `loading`, `empty`, `success` y `error`.
- Persistencia del token entre recargas y cierre controlado de sesión ante `401`/`403`.
- `/login` es la única pantalla pública; un guard padre cubre catálogo, detalle,
  favoritos, administración y rutas no encontradas.
- Shell responsive mobile-first con header, contenido y footer coordinados por `100dvh`;
  las convenciones de layout y su validación se documentan en `FRONTEND_UI.md`.

## Despliegue implementado

Docker Compose define frontend, backend, PostgreSQL 10, Neo4j 4.4, Kafka 2.0.1 y
ZooKeeper, con health checks, configuración por entorno y versiones fijadas. La pila se
validó con los seis servicios saludables sobre volúmenes locales existentes; queda
ejecutar y registrar el mismo escenario desde una base limpia. El healthcheck del
frontend usa `127.0.0.1` para no depender de la resolución IPv6 de `localhost` en la
imagen Alpine de Nginx.
