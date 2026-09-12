---
name: no-run-commands
description: Prohíbe al agente ejecutar el backend, el frontend, docker compose, builds pesados y tests. El humano ejecuta y pega la salida relevante. Aplica siempre en este repositorio; objetivo principal, reducir consumo de tokens y tiempo.
---

# no-run-commands

## Regla
El agente **nunca** ejecuta, directa ni indirectamente (scripts, `npm run`, Makefiles, `&&`, background):

### Backend
`mvn`/`./mvnw` en cualquier goal (`compile`, `package`, `test`, `verify`, `spring-boot:run`, `install`), `java -jar`, `gradle`.

### Frontend
`ng serve`, `ng build`, `ng test`, `ng e2e`, `npm start`, `npm run <cualquiera>`, `npm test`, `npx ...` que arranque procesos, `vitest`, `karma`, `jest`.

### Infraestructura
`docker`, `docker compose`/`docker-compose` (`up`, `down`, `build`, `logs`, `exec`, `config`), `kafka-*`, `psql`, `cypher-shell`, `curl` contra servicios locales.

### Instalación
`npm install`/`npm ci`, `mvn dependency:*`, `brew`, `apt`, `pip`, salvo petición explícita del humano en el mismo mensaje.

## Permitido
Leer y escribir ficheros; `ls`, `find`, `grep`, `cat`, `sed` sobre el repo; `git` de solo lectura ([no-git-write](../no-git-write/SKILL.md)).

## Qué hacer en su lugar
Al final de cada paso, entregar un bloque **"Ejecuta y pega"** con:
1. El comando exacto, uno por bloque `bash`.
2. Qué parte de la salida pegar (p. ej. "solo las líneas `[ERROR]` o `Tests run:`", "las 30 últimas líneas del log del servicio `kafka`").
3. Qué resultado se espera.

Al recibir la salida, analizarla y corregir. No pedir la salida completa si basta un fragmento.

## Por qué
- Los tests y builds producen miles de líneas: cargarlas en el contexto multiplica el coste.
- Los servicios (Kafka, Neo4j, Postgres) tardan y su log es ruidoso.
- El candidato debe ejecutar y entender su propio proyecto ([spec/10](../../spec/10-entrega.md): entrevista).

## Si el humano lo pide explícitamente
Recordar esta regla en una frase y pedir que ejecute él el comando. Solo si insiste en el mismo hilo con instrucción inequívoca puede ejecutarse **un** comando concreto, nunca en background y nunca `docker compose up`.
