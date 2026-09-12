# docker-compose

Fichero único en la raíz: `docker-compose.yml` (Compose v2, sin `version:`). Requisito: [spec/10-entrega.md](../spec/10-entrega.md). Imágenes: [stack-versiones.md](stack-versiones.md).

## Servicios
| Servicio | Puerto host | Depende de | Healthcheck |
|---|---|---|---|
| `postgres` | 5432 | — | `pg_isready` |
| `neo4j` | 7474, 7687 | — | HTTP 7474 |
| `zookeeper` | 2181 | — | `nc -z localhost 2181` |
| `kafka` | 9092 | zookeeper | `kafka-topics --list --zookeeper zookeeper:2181` (2.0.1 usa `--zookeeper`, no `--bootstrap-server` en algunas herramientas) |
| `backend` | 8080 | postgres, neo4j, kafka (`condition: service_healthy`) | `/actuator/health` |
| `frontend` | 4200 → 80 | backend | — |

## Reglas
- Versiones de imagen **fijadas** (nunca `latest`).
- Credenciales en `.env` con valores de desarrollo versionados (es una prueba; documentar en README).
- Volúmenes con nombre para Postgres y Neo4j: `docker compose down -v` reinicia limpio.
- Backend y frontend con `Dockerfile` multi-stage; `docker compose up --build` es el único comando necesario.
- Kafka: `KAFKA_ADVERTISED_LISTENERS` con listener interno (`kafka:29092`) y externo (`localhost:9092`) para poder ejecutar el backend fuera de Docker en desarrollo.
- Sync inicial: no automática en el arranque por defecto (propiedad `sync.on-startup=false`); README explica `POST /api/admin/sync`.

Relacionado: [workflows/01-infraestructura-docker.md](../workflows/01-infraestructura-docker.md), [kafka-2.0.1.md](kafka-2.0.1.md).
