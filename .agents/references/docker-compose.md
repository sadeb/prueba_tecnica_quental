# docker-compose

Fichero único: `projects/docker-compose.yml` (Compose v2, sin `version:`, `name: rickmorty`), con `projects/.env` al lado. Requisito: [spec/10-entrega.md](../spec/10-entrega.md). Imágenes y justificación: [stack-versiones.md](stack-versiones.md), [ADR-008](../decisions/ADR-008-imagenes-docker.md).

## Servicios
| Servicio | Imagen | Puerto host | Depende de | Healthcheck | Límite RAM |
|---|---|---|---|---|---|
| `postgres` | `postgres:10.23-alpine` | 5432 | — | `pg_isready -h 127.0.0.1 -U … -d …` (`-h` fuerza TCP: evita el falso OK del servidor temporal de `initdb`) | 256M |
| `neo4j` | `neo4j:4.4.48-community` | 7474, 7687 | — | `wget --spider http://127.0.0.1:7474` | 640M |
| `zookeeper` | `zookeeper:3.4.13` | — (no publicado) | — | `echo srvr \| nc -w 2 127.0.0.1 2181 \| grep Mode` | 256M |
| `kafka` | `wurstmeister/kafka:2.12-2.0.1` | 9092 | zookeeper | `kafka-broker-api-versions.sh --bootstrap-server 127.0.0.1:29092` (en 2.0.1 `kafka-topics --zookeeper` solo consulta ZK, no el broker) | 640M |
| `backend` | build `./backend` | 8080 | postgres, neo4j, kafka (`condition: service_healthy`) | `curl -f /actuator/health` | 512M |
| `frontend` | build `./frontend` | 4200 → 80 | backend (`service_healthy`) | `wget --spider http://127.0.0.1/` | 32M |

Todos los healthchecks: `interval: 30s` en régimen y `start_interval` de 2-5 s durante `start_period` (Docker ≥ 25). Kafka y Neo4j con `stop_grace_period: 30s`. Todos con `restart: unless-stopped` y rotación de logs (`x-logging`, 5 MB × 2).

## Reglas
- Versiones de imagen **fijadas** hasta el patch (nunca `latest`) y **multi-arch** (amd64 + arm64: el host de desarrollo es Apple Silicon).
- Credenciales en `projects/.env` con valores de desarrollo versionados (es una prueba; documentar en README). `.env` solo interpola; nada de `env_file:` (inyectaría `AUTH_TOKEN_SECRET` en Postgres).
- Volúmenes con nombre para Postgres, Neo4j y **ZooKeeper + Kafka juntos** (si solo persistiera Kafka, tras `down`+`up` el broker arrancaría con logs que ZK no conoce). `docker compose down -v` reinicia limpio.
- `NEO4J_AUTH` solo se aplica en la primera inicialización del volumen: cambiar `NEO4J_PASSWORD` exige `down -v`.
- Backend y frontend con `Dockerfile` multi-stage; `cd projects && docker compose up --build` es el único comando necesario.
- Kafka: `KAFKA_ADVERTISED_LISTENERS` con listener interno (`kafka:29092`) y externo (`localhost:9092`) para poder ejecutar el backend fuera de Docker en desarrollo. `auto.create.topics.enable=false`: los topics y sus `.DLT` los declara la app con `NewTopic`; `SPRING_KAFKA_ADMIN_FAIL_FAST=true` para que el backend no arranque sin topics.
- Mínimo consumo: heaps acotados (Neo4j 256m + pagecache 64m, Kafka 128-256m, ZK 128m, backend 50 % del límite con Serial GC y C1), Postgres `shared_buffers=32MB`/`max_connections=30`, Hikari 5, Tomcat 16 hilos, nginx con 1 worker. Suma de límites ≈ 2,3 GB. Sin límites de CPU (evitan throttling en arranques).
- Sync inicial: no automática en el arranque por defecto (`SYNC_ON_STARTUP=false`); README explica `POST /api/admin/sync`.
- Sin orquestación extra (Swarm, k8s, múltiples ficheros compose).

Relacionado: [workflows/01-infraestructura-docker.md](../workflows/01-infraestructura-docker.md), [kafka-2.0.1.md](kafka-2.0.1.md).
