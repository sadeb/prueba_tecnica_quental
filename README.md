# prueba_tecnica_quental

Prueba técnica Full Stack (Java / Angular) para Quental. Documentación completa de contexto en [AGENTS.md](AGENTS.md) y [.agents/](.agents/README.md).

**Fundamentación técnica del backend** (cada decisión de desarrollo y de arquitectura, revisiones de calidad por paso y verificación): [docs/decisiones-tecnicas-backend.md](docs/decisiones-tecnicas-backend.md). Los ADRs formales están en [.agents/decisions/](.agents/decisions/README.md).

## Qué hace
Flujo: API pública Rick and Morty → productor (descarga paginada, validación, mapeo) → Kafka (`rm.locations`, `rm.episodes`, `rm.characters` + `.DLT`) → consumidor (upsert idempotente) → PostgreSQL (atributos, usuarios, favoritos) y Neo4j (grafo de relaciones) → API REST propia con OpenAPI → SPA Angular 22 + Bootstrap 5.3 (`projects/frontend`).

## Requisitos previos
- Docker Desktop (Docker ≥ 25, Compose v2). Opcional para desarrollo: JDK 11, Maven 3.8+ y Node 24.

## Ejecución
Toda la infraestructura (PostgreSQL 10, Neo4j 4.4, ZooKeeper, Kafka 2.0.1), el backend y el frontend se levantan con un único comando desde `projects/`:

```bash
cd projects && docker compose up --build
```

PostgreSQL, Neo4j, ZooKeeper y Kafka están bajo el perfil de compose `infra`, activado por defecto mediante `COMPOSE_PROFILES=infra` en [projects/.env](projects/.env). Si se sobrescribe esa variable hay que pasar `--profile infra` explícitamente.

Credenciales de desarrollo en [projects/.env](projects/.env) (versionadas a propósito). Puertos publicados en el host (los del compose; los internos son los estándar):

| Servicio | URL en el host |
|---|---|
| Frontend | http://localhost:4300 |
| API REST | http://localhost:4080/api |
| OpenAPI JSON / Swagger UI | http://localhost:4080/v3/api-docs · http://localhost:4080/swagger-ui.html |
| Salud del backend | http://localhost:4080/actuator/health |
| Neo4j Browser (bolt en 4788) | http://localhost:4575 |
| PostgreSQL | `localhost:4432` |
| Kafka (desde el host) | `localhost:4093` (ver limitación abajo) |

Solo la infraestructura (para ejecutar backend/frontend fuera de Docker):

```bash
cd projects && docker compose up -d postgres neo4j zookeeper kafka
```

Estado limpio:

```bash
cd projects && docker compose down -v
```

### Swagger / OpenAPI activable
`SWAGGER_ENABLED` (en [projects/.env](projects/.env), default `true`) enciende o apaga `/v3/api-docs` y `/swagger-ui.html`. Con `SWAGGER_ENABLED=false` ambas rutas responden 404 y no se crea configuración de OpenAPI ([ADR-009](.agents/decisions/ADR-009-openapi-toggle.md)).

## Usar la API
1. Registro e inicio de sesión:

```bash
curl -s -X POST http://localhost:4080/api/auth/register -H 'Content-Type: application/json' -d '{"username":"rick","password":"wubbalubba"}'
```

```bash
TOKEN=$(curl -s -X POST http://localhost:4080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"rick","password":"wubbalubba"}' | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])'); echo $TOKEN
```

2. Lanzar la sincronización (requiere token; en un entorno real exigiría rol administrador) y consultar su estado:

```bash
curl -s -X POST http://localhost:4080/api/admin/sync -H "Authorization: Bearer $TOKEN"
```

```bash
curl -s http://localhost:4080/api/admin/sync/1 -H "Authorization: Bearer $TOKEN"
```

La sync es asíncrona: `status` pasa de `RUNNING` a `COMPLETED` (o `PARTIAL` si alguna página falló). Contadores: `publishedMessages`, `skippedItems`, `failedPages`, `failedMessages` (mensajes enviados a DLT). Reejecutarla es seguro: los upserts son idempotentes.

3. Consultar datos (público) y favoritos (token):

```bash
curl -s "http://localhost:4080/api/characters?name=rick&status=alive&page=0&size=5"
```

```bash
curl -s "http://localhost:4080/api/characters/1/related?limit=5"
```

```bash
curl -s -X POST http://localhost:4080/api/users/me/favorites/1 -H "Authorization: Bearer $TOKEN"
```

Contrato completo de endpoints y códigos: [.agents/conventions/api-rest.md](.agents/conventions/api-rest.md); formato de error único: [formato-error.md](.agents/conventions/formato-error.md).

## Desarrollo local del backend
Con la infraestructura levantada en Docker y JDK 11:

```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) mvn -N wrapper:wrapper -Dmaven=3.9.16
```

```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw spring-boot:run
```

`application.yml` apunta por defecto a los puertos publicados por el compose (Postgres 4432, Neo4j 4788, Kafka 4093). **Limitación conocida**: el broker anuncia `localhost:9092` (`KAFKA_ADVERTISED_LISTENERS`) mientras el puerto publicado es 4093, así que un backend en el host se conecta pero es redirigido a 9092 y no puede producir ni consumir. Opciones: ejecutar el backend dentro del compose (recomendado) o cambiar el listener `EXTERNAL` anunciado a `localhost:4093`.

## Tests del backend
Sin Docker ni red: H2 en modo PostgreSQL, Kafka embebido y Neo4j sustituido por un doble.

```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw test
```

`SyncFlowIT` es la prueba del ciclo de sincronización: publica en Kafka embebido, el consumidor real persiste en H2, se comprueba que reprocesar el mismo mensaje no duplica y que un mensaje corrupto acaba en `rm.characters.DLT` sin bloquear el siguiente.

## Frontend (Angular 22 + Bootstrap 5.3)
SPA en `projects/frontend/` (Angular 22.1, standalone, zoneless, Signal Forms, Bootstrap 5.3.8 solo CSS). Con el compose levantado se sirve en http://localhost:4300 (nginx hace proxy de `/api` al backend).

- **Acceso**: `/login` es la única ruta pública; el registro es una pestaña de esa misma pantalla. Todo lo demás exige sesión (guardas `canActivate` + `canActivateChild`) y el token viaja en cada petición (`Authorization: Bearer`) gracias al interceptor.
- **Pantallas**: listado de personajes con filtros (nombre, estado, especie, género) y paginación en la URL; detalle con origen, localización, episodios y personajes relacionados (grafo); favoritos con actualización optimista; 404 propio.
- **Errores**: timeout de 15 s, reintento automático de lecturas ante fallos transitorios, conversión centralizada a `ApiError` con mensaje en español (contextualizado: credenciales, personaje inexistente, grafo caído, usuario en uso…), sesión caducada → vuelta a `/login` conservando la URL, `ErrorHandler` global con toast. Cada vista renderiza sus cuatro estados (`loading`, `empty`, `error`, `ready`).
- **Interfaz**: responsiva (móvil → escritorio), tema claro/oscuro con conmutador persistente y respeto a la preferencia del sistema, accesibilidad (etiquetas, `aria-live`, foco en errores, enlace "saltar al contenido"). Textos en español; los datos de la API de Rick and Morty se muestran sin traducir (solo los enumerados `status`/`gender` tienen etiqueta en español).

Desarrollo local (Node 24, backend en el puerto 4080 vía compose; `proxy.conf.json` redirige `/api`):

```bash
cd projects/frontend && npm ci
```

```bash
cd projects/frontend && npm start
```

Tests del frontend (Vitest + jsdom, sin red; 67 casos: guardas, interceptores, sesión, servicios, filtros con debounce, listado, login/registro, paginación, tema):

```bash
cd projects/frontend && npx ng test --watch=false
```

Un solo fichero, p. ej. el interceptor de errores:

```bash
cd projects/frontend && npx ng test --watch=false --include='**/error.interceptor.spec.ts'
```

Build de producción (lo que hace el Dockerfile):

```bash
cd projects/frontend && npx ng build
```

Decisiones del frontend: [ADR-007](.agents/decisions/ADR-007-best-practices-angular.md) (buenas prácticas oficiales) y [ADR-010](.agents/decisions/ADR-010-frontend-sesion-tema-errores.md) (entrada única, sesión, tema, idioma y errores).

## Decisiones de diseño
Resumen por ADR (detalle y justificación de desarrollo en [docs/decisiones-tecnicas-backend.md](docs/decisiones-tecnicas-backend.md)):
- [ADR-001](.agents/decisions/ADR-001-identificador-externo.md) · id interno + `external_id` único como clave de correspondencia Postgres ↔ Neo4j; `""` → `null`; referencias sin URL → `null`.
- [ADR-002](.agents/decisions/ADR-002-idempotencia-sync.md) · mensajes = snapshot completo; upsert por `external_id`; relaciones N:M reemplazadas; `sync_runs` con contadores.
- [ADR-003](.agents/decisions/ADR-003-topics-kafka.md) · un topic por entidad + DLT, clave = `external_id`, JSON versionado con el modelo propio.
- [ADR-004](.agents/decisions/ADR-004-consistencia-postgres-neo4j.md) · Postgres primero, Neo4j después; el reintento es la reconciliación; placeholders para referencias adelantadas.
- [ADR-005](.agents/decisions/ADR-005-autenticacion.md) · token propio HMAC-SHA256 sin librerías; Spring Security solo para BCrypt, filtro y rutas.
- [ADR-006](.agents/decisions/ADR-006-mensajes-irrecuperables.md) · `DefaultErrorHandler` + DLT por topic; errores de formato sin reintento; contador `failed_messages`.
- [ADR-008](.agents/decisions/ADR-008-imagenes-docker.md) · imágenes multi-arch fijadas, healthchecks reales, límites de memoria.
- [ADR-009](.agents/decisions/ADR-009-openapi-toggle.md) · Swagger/OpenAPI activable por `SWAGGER_ENABLED`.
- [ADR-010](.agents/decisions/ADR-010-frontend-sesion-tema-errores.md) · frontend: `/login` única ruta pública, interceptores auth/retry/error, `ApiError` en español por contexto, tema claro/oscuro persistido.

## Limitaciones conocidas y siguientes pasos
- Sin roles: `POST /api/admin/sync` lo puede lanzar cualquier usuario autenticado. Sin refresh token ni revocación.
- Reproceso del DLT fuera de alcance: inspeccionar con `kafka-console-consumer.sh --bootstrap-server kafka:29092 --topic rm.characters.DLT --from-beginning` dentro del contenedor `kafka`.
- Kafka desde el host: ver limitación del listener anunciado.
- Pendiente: comprobación manual del frontend contra el backend levantado y cierre del README de entrega (workflow 18).

## Estructura del repositorio
Ver [.agents/conventions/estructura-repositorio.md](.agents/conventions/estructura-repositorio.md): `projects/backend` (Spring Boot), `projects/frontend` (Angular), `projects/docker-compose.yml` + `.env`, `docs/` (fundamentación técnica), `.agents/` (contexto para agentes).
