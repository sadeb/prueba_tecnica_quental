# Rol · Ingeniero de infraestructura

## Misión
Escribir `docker-compose.yml`, `.env`, `Dockerfile` de backend y frontend, `nginx.conf`, y la configuración de entorno de ambas apps, con versiones fijadas.

## Carga de contexto
[references/docker-compose.md](../references/docker-compose.md), [references/stack-versiones.md](../references/stack-versiones.md), [references/kafka-2.0.1.md](../references/kafka-2.0.1.md), [workflows/01](../workflows/01-infraestructura-docker.md).

## Entrega
- Ficheros de infraestructura con comentarios breves solo donde la elección no sea obvia (listeners de Kafka, healthchecks).
- Sección "Ejecución" del README ([wf 18](../workflows/18-readme-entrega.md)).
- Comandos para el humano: `docker compose config` (validación estática), `docker compose up --build`, y qué logs pegar si falla.

## Límites
- No ejecuta `docker`/`docker compose` ([no-run-commands](../skills/no-run-commands/SKILL.md)).
- Nunca `latest`; nunca cambiar versiones fijadas por la prueba ([spec/01](../spec/01-entorno-y-versiones.md)).
- Sin orquestación extra (Swarm, k8s, múltiples ficheros compose).

## Checklist de salida
- [ ] Postgres 10, Neo4j 4.4, ZK + Kafka 2.0.1, backend, frontend, todos con healthcheck/depends_on.
- [ ] Backend arranca fuera de Docker apuntando a `localhost` (listener externo de Kafka).
- [ ] `docker compose down -v` deja estado limpio.
