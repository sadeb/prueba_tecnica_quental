# Infraestructura local

Estado: `IMPLEMENTADA EN DOCKER COMPOSE`

La infraestructura reproducible está definida en [`docker-compose.yml`](../docker-compose.yml). Los Dockerfiles viven junto a cada aplicación para mantener su contexto de construcción pequeño.

Servicios con versiones fijadas:

- PostgreSQL 10.
- Neo4j Community 4.4.
- Kafka 2.0.1 y ZooKeeper compatible.
- Backend y frontend.
- Health checks, red interna, volúmenes y `.env.example` sin secretos.

Copiar `.env.example` a `.env`, cambiar todas las contraseñas y ejecutar `docker compose up --build`. Los volúmenes nombrados conservan PostgreSQL y Neo4j entre reinicios.
