# prueba_tecnica_quental

Prueba técnica Full Stack (Java / Angular) para Quental. Documentación completa de contexto en [AGENTS.md](AGENTS.md) y [.agents/](.agents/README.md).

## Requisitos previos
- Docker Desktop (Docker ≥ 25, Compose v2). Opcional para desarrollo: JDK 11 y Node 24.

## Ejecución
Toda la infraestructura (PostgreSQL 10, Neo4j 4.4, ZooKeeper, Kafka 2.0.1), el backend y el frontend se levantan con un único comando desde `projects/`:

```bash
cd projects && docker compose up --build
```

Credenciales de desarrollo en [projects/.env](projects/.env) (versionadas a propósito).

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API REST | http://localhost:8080/api |
| OpenAPI (Swagger UI) | http://localhost:8080/swagger-ui.html |
| Neo4j Browser | http://localhost:7474 |
| Kafka (desde el host) | `localhost:9092` |

Solo la infraestructura (para ejecutar backend/frontend fuera de Docker):

```bash
cd projects && docker compose up -d postgres neo4j zookeeper kafka
```

Estado limpio:

```bash
cd projects && docker compose down -v
```

## Pendiente
Secciones de sincronización, desarrollo local, tests, decisiones de diseño y limitaciones: se completan en [workflows/18](.agents/workflows/18-readme-entrega.md). Imágenes y decisiones de infraestructura: [ADR-008](.agents/decisions/ADR-008-imagenes-docker.md).
