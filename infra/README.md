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

Puertos publicados en el host:

| Servicio | Host | Contenedor |
| --- | ---: | ---: |
| Frontend HTTP | `4400` | `80` |
| Backend HTTP | `4889` | `8989` |
| PostgreSQL | `4532` | `5432` |
| Neo4j Browser | `4774` | `7474` |
| Neo4j Bolt | `4787` | `7687` |
| Kafka | `4992` | `9092` |

Los contenedores se comunican mediante sus nombres de servicio y puertos internos. Los
procesos ejecutados directamente en el host deben usar los puertos publicados de la
tabla. Kafka escucha internamente en `29092` para otros contenedores y en `9092` para
el listener publicado; anuncia `kafka:29092` internamente y `localhost:4992` a los
clientes ejecutados en el host.
