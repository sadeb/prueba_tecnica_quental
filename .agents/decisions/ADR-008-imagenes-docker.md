# ADR-008 · Imágenes Docker, healthchecks y límites de recursos

**Estado**: Propuesta
**Requisito**: [spec/01-entorno-y-versiones.md](../spec/01-entorno-y-versiones.md), [spec/10-entrega.md](../spec/10-entrega.md)
**Fecha**: 2026-09-13

## Contexto
El enunciado fija versiones (JDK 11, Boot 2.7, Postgres 10, Kafka 2.12-2.0.1) y exige un `docker-compose` con versiones fijadas. El host de desarrollo es Apple Silicon (arm64), así que cualquier imagen solo amd64 correría emulada. Se quiere el mínimo consumo de recursos sin perder fiabilidad en el arranque ordenado.

## Decisión
- Todas las imágenes fijadas hasta el patch y **multi-arch**: `postgres:10.23-alpine`, `neo4j:4.4.48-community`, `zookeeper:3.4.13`, `wurstmeister/kafka:2.12-2.0.1`, `maven:3.9.16-eclipse-temurin-11` → `eclipse-temurin:11.0.31_11-jre-jammy`, `node:24.21.0-alpine` → `nginx:1.30.4-alpine-slim`.
- Dockerfiles multi-stage con cache de dependencias de BuildKit, jar de Boot extraído en capas (`layertools`), usuario no root en el backend y `nginx.conf` completo (1 worker, SPA fallback, proxy `/api` con `resolver` de Docker).
- Healthchecks que comprueban el servicio real: `pg_isready -h 127.0.0.1` (TCP, no socket), `srvr` en ZooKeeper, `kafka-broker-api-versions.sh` en Kafka (única herramienta de 2.0.1 que interroga al broker), HTTP en Neo4j/backend/frontend. `start_interval` corto durante el arranque y 30 s en régimen.
- Límites de memoria por servicio (`deploy.resources.limits`) con heaps JVM acotados; sin límites de CPU. Suma ≈ 2,3 GB.
- ZooKeeper y Kafka se persisten juntos en volúmenes con nombre; `KAFKA_LOG_DIRS` fijo.

## Alternativas descartadas
- `confluentinc/cp-kafka:5.0.1` + `cp-zookeeper:5.0.1` — equivalen a Kafka 2.0.1 pero solo amd64 (emulación en Apple Silicon) y sin actualizar desde 2018.
- `eclipse-temurin:11-jre-alpine` (60 MB) y `maven:…-alpine` — solo amd64.
- `zookeeper:3.4.14` — solo amd64; 3.4.13 es además la versión que empaqueta Kafka 2.0.1.
- `neo4j:5.x` — SDN 6.3 (Boot 2.7.18) está probado contra 4.4; 5.x no aporta nada al alcance.
- `node:20`/`node:22` — Node 20 está fuera de soporte y Angular 22 exige `^22.22.3 || ^24.15.0`; 24 es la LTS activa.
- Healthcheck de Kafka con `kafka-topics.sh --zookeeper` — solo consulta ZooKeeper; el broker podría estar caído y el servicio aparecer `healthy`.
- `./mvnw` dentro de la imagen — el `.gitignore` raíz ignora `*.jar` y el wrapper descargaría Maven en cada build; la imagen `maven` fija la versión explícitamente.

## Consecuencias
Gana: arranque nativo en arm64 y amd64, imágenes runtime pequeñas (backend ≈ 85 MB base, frontend ≈ 9 MB), arranque ordenado fiable y stack completo por debajo de 2,5 GB. Cuesta: el healthcheck de Kafka lanza una JVM cada 30 s (mitigado con `-Xmx48m`); `wurstmeister/kafka` no se actualiza desde 2022 (aceptable: la versión del broker la fija el enunciado). Vigilar: la ruta `dist/frontend/browser` y la salida de `ng new` (sin SSR). En entrevista: "cada imagen es la más ligera que existe para la versión exigida en las dos arquitecturas, y cada healthcheck comprueba el servicio real, no un puerto".

## Dónde se aplica
`projects/docker-compose.yml`, `projects/backend/Dockerfile`, `projects/frontend/Dockerfile`, `projects/frontend/nginx.conf`; [workflows/01](../workflows/01-infraestructura-docker.md), [references/docker-compose.md](../references/docker-compose.md), [references/stack-versiones.md](../references/stack-versiones.md).
