# Infraestructura reservada

Estado: `NO GENERADA`

Este directorio está reservado para la futura infraestructura reproducible. Durante `CONTEXT_BOOTSTRAP` no debe contener Dockerfiles, `docker-compose.yml`, scripts de arranque, volúmenes ni datos locales.

La Fase 3 preparará, con versiones fijadas y verificadas:

- PostgreSQL 10.
- Neo4j Community 4.4.
- Kafka 2.0.1 y ZooKeeper compatible.
- Backend y frontend.
- Health checks, red interna, volúmenes y `.env.example` sin secretos.

La infraestructura solo podrá crearse después de autorización explícita y actualización de [AGENTS.md](../AGENTS.md), [roadmap](../.agents/context/ROADMAP.md) y [handoff](../.agents/context/HANDOFF.md).
