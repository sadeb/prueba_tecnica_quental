# 01 · Infraestructura docker-compose

**Rol**: [ingeniero-infra](../agents/ingeniero-infra.md)

## Objetivo
`docker-compose.yml` con PostgreSQL 10, Neo4j 4.4, Zookeeper y Kafka 2.0.1, más servicios `backend` y `frontend` (estos últimos pueden quedar comentados hasta que existan sus Dockerfiles).

## Contexto
[references/docker-compose.md](../references/docker-compose.md), [references/kafka-2.0.1.md](../references/kafka-2.0.1.md), [references/stack-versiones.md](../references/stack-versiones.md), [spec/10](../spec/10-entrega.md).

## Pasos
1. Servicios `postgres`, `neo4j`, `zookeeper`, `kafka` con imágenes fijadas, variables desde `.env`, volúmenes con nombre y healthchecks.
2. Kafka con dos listeners (interno `kafka:29092`, externo `localhost:9092`) y `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1`.
3. Placeholders de `backend` (puerto 8080, `depends_on` con `condition: service_healthy`, variables `SPRING_DATASOURCE_URL`, `SPRING_NEO4J_URI`, `SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092`, `AUTH_TOKEN_SECRET`) y `frontend` (puerto 4200:80).
4. Red por defecto; sin `version:`.

## Hecho cuando
`docker compose config` valida sin errores (lo ejecuta el humano); los cuatro servicios de infraestructura pasan a `healthy`.

## Ejecuta y pega
```bash
docker compose config --quiet && echo OK
```
```bash
docker compose up -d postgres neo4j zookeeper kafka && docker compose ps
```
Pegar solo la tabla de `ps` (columna STATUS) y, si algo no está `healthy`, las 30 últimas líneas de `docker compose logs <servicio>`.

## Commit propuesto
`chore(infra): add docker-compose with postgres 10, neo4j 4.4, zookeeper and kafka 2.0.1`
