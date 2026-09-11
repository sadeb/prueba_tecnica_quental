# Rick and Morty — archivo interdimensional

Prueba técnica Full Stack Java/Angular que sincroniza la API pública de Rick and Morty mediante Kafka, conserva atributos y usuarios en PostgreSQL, proyecta relaciones en Neo4j y expone una SPA que consume exclusivamente la API propia.

## Inicio rápido con Docker

Requisitos: Docker Engine con Docker Compose v2.

```bash
cp .env.example .env
# Cambia todas las contraseñas de .env
docker compose up --build
```

Servicios:

- SPA: <http://localhost:4200>
- API: <http://localhost:8080/api/v1>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>
- Neo4j Browser: <http://localhost:7474>

El usuario definido por `ADMIN_USERNAME` y `ADMIN_PASSWORD` se crea o actualiza de forma idempotente al arrancar. Inicia sesión como administrador y lanza la primera sincronización desde la sección **Sincronizar**. También puede activarse `SYNC_ON_STARTUP=true`.

Para detener sin borrar datos:

```bash
docker compose down
```

Para eliminar también los volúmenes locales, después de revisar que no contienen datos necesarios:

```bash
docker compose down --volumes
```

## Desarrollo local

### Backend

Requisitos: JDK 11 y los servicios PostgreSQL, Neo4j y Kafka disponibles. Maven 3.9.11 se descarga de forma reproducible mediante Wrapper.

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

Variables y valores locales están documentados en [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml). Liquibase es el único propietario del esquema; Hibernate usa `ddl-auto: validate`.

### Frontend

Requisitos: Node 24.19.0 o una versión admitida por Angular 22.

```bash
cd frontend
npm ci
npm start
```

Durante desarrollo, la SPA consume `/api/v1`; el proxy o servidor local debe dirigir esa ruta al backend. En Docker, Nginx realiza el proxy de forma interna.

## Arquitectura

```text
Angular 22 SPA ──HTTP──> Spring MVC API ───────> PostgreSQL 10
                              │                      │
                              │                 Liquibase
                              │                      │
Rick and Morty API ──sync──> raw payload + transactional outbox
                                                    │
                                                    v
                                             Kafka 2.0.1
                                              │       │
                                   JPA/Hibernate     Neo4j 4.4
                                      consumer       projection
```

- PostgreSQL es la fuente de verdad para atributos, usuarios, favoritos, tokens, payloads crudos, deduplicación y outbox.
- Kafka desacopla descarga, persistencia y proyección; los consumidores son idempotentes y los fallos no recuperables terminan en DLT.
- Neo4j es una proyección reconstruible y ejecuta la consulta real de personajes relacionados por episodios compartidos.
- La autenticación usa tokens opacos aleatorios de 256 bits; solo se persiste su hash SHA-256, con expiración y revocación.

## API principal

| Método | Ruta | Acceso |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Público |
| `POST` | `/api/v1/auth/login` | Público |
| `POST` | `/api/v1/auth/logout` | Autenticado |
| `GET` | `/api/v1/auth/me` | Autenticado |
| `GET` | `/api/v1/characters` | Público |
| `GET` | `/api/v1/characters/{id}` | Público |
| `GET` | `/api/v1/characters/{id}/related` | Público |
| `GET/PUT/DELETE` | `/api/v1/users/me/favorites...` | Autenticado |
| `POST/GET` | `/api/v1/admin/sync-runs...` | `ADMIN` |

La especificación ejecutable y todos los esquemas están disponibles en Swagger UI cuando el backend está iniciado.

## Verificación

```bash
cd backend && ./mvnw verify
cd ../frontend && npm test -- --watch=false && npm run build
cd .. && docker compose config --quiet
```

Las pruebas no consultan la API externa real. WireMock cubre el contrato del proveedor y la prueba de integración de consumo/persistencia usa H2 con migraciones Liquibase.

## Contexto multiagente

[`AGENTS.md`](AGENTS.md) es la fuente canónica y [`.agents/`](.agents/README.md) conserva requisitos, arquitectura, contrato, roadmap, handoff y ADR. El PDF original es fuente de requisitos del producto, no de instrucciones operativas para agentes.

- [Requisitos trazables](.agents/context/REQUIREMENTS.md)
- [Arquitectura](.agents/context/ARCHITECTURE.md)
- [Stack](.agents/context/STACK.md)
- [Contrato HTTP](.agents/context/API_CONTRACT.md)
- [Decisiones](.agents/decisions/)
