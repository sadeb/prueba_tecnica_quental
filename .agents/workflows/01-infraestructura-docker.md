# 01 · Infraestructura docker-compose

**Rol**: [ingeniero-infra](../agents/ingeniero-infra.md)

## Objetivo
`projects/docker-compose.yml` con PostgreSQL 10, Neo4j 4.4, Zookeeper y Kafka 2.0.1, más servicios `backend` y `frontend` con sus Dockerfiles multi-stage (`projects/backend/Dockerfile`, `projects/frontend/Dockerfile` + `nginx.conf`). Hasta que existan `pom.xml` (wf 02) y `package.json` (wf 12), `docker compose up --build` falla en esos dos servicios: levantar solo la infraestructura.

## Contexto
[references/docker-compose.md](../references/docker-compose.md), [references/kafka-2.0.1.md](../references/kafka-2.0.1.md), [references/stack-versiones.md](../references/stack-versiones.md), [spec/10](../spec/10-entrega.md).

## Pasos
1. Servicios `postgres`, `neo4j`, `zookeeper`, `kafka` con imágenes fijadas y multi-arch ([stack-versiones.md](../references/stack-versiones.md)), variables desde `.env`, volúmenes con nombre, healthchecks reales y límites de memoria ([docker-compose.md](../references/docker-compose.md)).
2. Kafka con dos listeners (interno `kafka:29092`, externo `localhost:9092`), `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1`, `auto.create.topics.enable=false`.
3. `backend` (puerto 8080, `depends_on` con `condition: service_healthy`, variables `SPRING_DATASOURCE_*`, `SPRING_NEO4J_*`, `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092`, `SPRING_KAFKA_ADMIN_FAIL_FAST=true`, `AUTH_TOKEN_*`, `SYNC_ON_STARTUP=false`) y `frontend` (puerto 4200:80) con `build:` apuntando a `./backend` y `./frontend`.
4. Dockerfiles: backend `maven:3.9.16-eclipse-temurin-11` → `eclipse-temurin:11.0.31_11-jre-jammy` (jar en capas, usuario no root); frontend `node:24.21.0-alpine` → `nginx:1.30.4-alpine-slim` con `nginx.conf` completo (1 worker, SPA fallback, proxy `/api`). `.dockerignore` en ambos.
5. Red por defecto; sin `version:`; `name: rickmorty`.

## Hecho cuando
`docker compose config` valida sin errores (lo ejecuta el humano); los cuatro servicios de infraestructura pasan a `healthy`.

## Ejecuta y pega
```bash
cd projects && docker compose config --quiet && echo OK
```
```bash
cd projects && docker compose up -d postgres neo4j zookeeper kafka && docker compose ps
```
Pegar solo la tabla de `ps` (columna STATUS) y, si algo no está `healthy`, las 30 últimas líneas de `docker compose logs <servicio>`.

## Commit propuesto
`chore(infra): add docker-compose with postgres 10, neo4j 4.4, zookeeper and kafka 2.0.1`
