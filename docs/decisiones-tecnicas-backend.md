# Fundamentación técnica del backend

Documento vivo que justifica **cada decisión** tomada al construir el servicio backend (`projects/backend/`), tanto de desarrollo como de arquitectura, y recoge la **revisión de calidad y de correspondencia con el requisito** hecha tras cada paso. Se actualiza con cada workflow completado.

Cómo leerlo: los requisitos viven en [`.agents/spec/`](../.agents/spec/README.md) y las decisiones de arquitectura formales en los ADR de [`.agents/decisions/`](../.agents/decisions/README.md); aquí no se copian, se enlazan y se baja al nivel de código: qué clase resuelve cada punto, por qué así y no de otra forma, qué encontró la revisión y cómo se verifica.

Estado: código escrito y revisado estáticamente; **pendiente de compilar y ejecutar por el humano** (el agente no ejecuta Maven ni Docker por regla del repositorio). Los comandos de cada sección son los que hay que ejecutar y qué parte de la salida pegar.

## 1. Arquitectura en una página

```
fuente externa ──> external/ (cliente + validación + mapeo) ──> sync/producer ──> Kafka rm.{locations,episodes,characters}
                                                                                          │
                                                                                          ▼
   API REST (/api/**) <── character|episode|location (query) <── PostgreSQL <── sync/consumer ──> Neo4j (graph/)
        │                                                                                  │
        └── user/ (favoritos) · auth/ (token propio) · common/ (ApiError) · config/         └── rm.*.DLT (irrecuperables)
```

| Requisito | Dónde se resuelve |
|---|---|
| [Req. 1 · integración externa](../.agents/spec/02-integracion-api-externa.md) | `external/`: `RickAndMortyClient`, `ExternalPayloadValidator`, `ExternalSnapshotMapper`, `ExternalIdParser` (§4) |
| [Req. 2 · sincronización Kafka](../.agents/spec/03-sincronizacion-kafka.md) | `sync/producer`, `sync/consumer`, `sync/message`, `config/Kafka*Config` (§5, §6) |
| [Req. 3 · modelo de datos](../.agents/spec/04-modelo-de-datos.md) | `db/migration/V1..V4`, entidades JPA, `graph/Neo4jGraphRepository` (§2, §3) |
| [Req. 4 · API propia](../.agents/spec/05-api-propia.md) | `auth/`, `user/`, `*QueryService` + `*Controller`, `common/GlobalExceptionHandler`, OpenAPI (§7, §8, §9, §10) |
| [Req. 6 · pruebas](../.agents/spec/07-pruebas.md) | `src/test` (§11) |
| Swagger activable por entorno (petición del candidato) | `SWAGGER_ENABLED` → springdoc ([ADR-009](../.agents/decisions/ADR-009-openapi-toggle.md), §10) |

Paquetes según [conventions/java-spring.md](../.agents/conventions/java-spring.md): dependencias hacia dentro (controlador → servicio → repositorio), el cliente externo solo lo usa `sync/producer`, DTOs separados de entidades, inyección por constructor, sin Lombok/MapStruct. Java 11 estricto: `maven.compiler.release=11` hace que un JDK 17 del host rechace APIs posteriores.

## 2. Workflow 02 · Esqueleto, configuración y errores

**Requisito**: [workflows/02](../.agents/workflows/02-backend-esqueleto.md); [formato-error.md](../.agents/conventions/formato-error.md).

### Decisiones
- **POM** (`pom.xml`): parent `spring-boot-starter-parent:2.7.18`; todas las versiones las gestiona el BOM salvo `springdoc-openapi-ui:1.8.0` (última release 1.x, octubre 2024; la referencia decía 1.7.0 y se ha actualizado). Kafka es `org.springframework.kafka:spring-kafka`: **no existe** `spring-boot-starter-kafka` aunque el workflow lo llamara "starter". `maven-surefire-plugin` incluye `**/*IT.java`: sin esa línea `SyncFlowIT` no se ejecutaría con `mvn test` y la prueba obligatoria quedaría silenciosamente fuera.
- **Gestor de transacciones explícito** (`config/PersistenceConfig`): con JPA y Neo4j en el classpath, Boot autoconfigura dos beans llamados `transactionManager`, ambos `@ConditionalOnMissingBean(TransactionManager)`; solo sobrevive uno (verificado en los jars de Boot 2.7.18) y los repositorios JPA podrían acabar sobre el gestor de Neo4j. Se declara `JpaTransactionManager` `@Primary`; Neo4j trabaja en auto-commit por sentencia (coherente con [ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md): no hay transacción distribuida).
- **Configuración** (`application.yml`): valores por defecto para desarrollo local contra los puertos que publica el compose (Postgres 4432, Neo4j 4788, Kafka 4093); dentro del compose todo llega por variables `SPRING_*`, `SYNC_ON_STARTUP`, `AUTH_TOKEN_*`, `SWAGGER_ENABLED` (relaxed binding, sin placeholders salvo los dos toggles). `open-in-view=false` (mapeo de relaciones LAZY dentro de servicios `readOnly`), `ddl-auto=validate` (el esquema lo posee Flyway), `throw-exception-if-no-handler-found=true` + `add-mappings=false` para que una ruta no mapeada produzca `ApiError` 404.
- **Propiedades tipadas** con `@ConfigurationProperties` + setters + `@EnableConfigurationProperties` en la configuración que las consume. Se evitó `@ConstructorBinding` (en Boot 2.7 prohíbe `@Component` y complica los slices) para eliminar riesgo de arranque.
- **Errores** (`common/GlobalExceptionHandler`): único productor de cuerpos de error; tabla de [formato-error.md](../.agents/conventions/formato-error.md) más 405 `METHOD_NOT_ALLOWED` y `DataIntegrityViolationException` → 409 (la restricción única es la última palabra ante duplicados concurrentes). `details` se omite cuando no hay errores de campo (`@JsonInclude(NON_NULL)`, decisión "omitir" fijada). Mensajes internos de Hibernate/Neo4j/Kafka nunca llegan al cliente: 503 genérico y `ERROR` en log.

### Alternativas descartadas
- Gradle: más superficie; Maven + wrapper es lo que espera el Dockerfile existente.
- `@ControllerAdvice` + `ResponseStatusException`: reparte el formato de error por el código; el requisito pide formato homogéneo.

### Revisión (estática, por el agente)
- ✔ Sin `record`, text blocks, `switch` con flechas, `Stream.toList`, `jakarta.*`, `@Autowired` en campos, `System.out`, Lombok (grep sobre `src/main`).
- ✘→✔ `InvalidTokenException` (401) no estaba mapeada en el handler: `AuthService.login` la lanza y habría salido como 500. Añadido `handleInvalidToken`.
- ✘→✔ `AuthService.register` y `FavoriteService.add` capturaban `DataIntegrityViolationException` dentro de la misma transacción: Hibernate marca la transacción *rollback-only* al fallar el flush y el commit posterior lanzaría `UnexpectedRollbackException` (500). Eliminado el `try/catch`; el caso concurrente lo resuelve el handler global con 409.

### Verificación (humano)
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) mvn -N wrapper:wrapper -Dmaven=3.9.16
```
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw -q -DskipTests compile && echo BUILD_OK
```
Pegar solo las líneas `[ERROR]`. Esperado: `BUILD_OK`.

Commit propuesto: `chore(backend): scaffold spring boot 2.7 project on jdk 11 with base config and error handling`

## 3. Workflow 06 · Persistencia PostgreSQL

**Requisito**: [spec/04](../.agents/spec/04-modelo-de-datos.md); [ADR-001](../.agents/decisions/ADR-001-identificador-externo.md), [ADR-002](../.agents/decisions/ADR-002-idempotencia-sync.md), [ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md).

### Decisiones
- **Esquema versionado con Flyway** (bonus B4): `V1__init.sql` (locations, episodes, characters, character_episodes), `V2__sync_runs.sql`, `V3__users.sql`, `V4__user_favorites.sql`. Cada tabla sincronizada: `id bigserial` + `external_id bigint UNIQUE` ([ADR-001](../.agents/decisions/ADR-001-identificador-externo.md)) + `placeholder boolean` ([ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md)) + `created_at/updated_at`. Columnas descriptivas **nullable** porque una fila puede nacer como placeholder solo con `external_id`.
- **DDL en el subconjunto común Postgres 10 / H2 2.1 `MODE=PostgreSQL`** (verificado contra H2 local): `bigserial`, `timestamp with time zone`, índices simples, FK con `on delete cascade`, `unique`. Excluidos a propósito: `timestamptz` (H2 lo rechaza), índices parciales o por expresión, `ON CONFLICT ... DO UPDATE`, `jsonb`. Precio: el filtro `name` usa `lower(name) LIKE` sin índice funcional; con 826 filas es irrelevante y se documenta.
- **Upsert en JPA, no en SQL**: buscar por `external_id` → crear o actualizar → `save`, todo en `@Transactional`. Es idempotente por construcción del estado final ([ADR-002](../.agents/decisions/ADR-002-idempotencia-sync.md)) y funciona igual en H2 y Postgres. Alternativa `ON CONFLICT DO UPDATE` nativo: más rápido pero no portable a los tests y obliga a SQL a mano para la N:M.
- **N:M `character_episodes`** como `@ManyToMany` con `Set<Episode>` y **reemplazo completo** del conjunto en cada upsert (`Character.applyRelations`): un personaje eliminado de un episodio en la fuente desaparece de la relación. Un solo "escritor" de la relación (el snapshot del personaje); el snapshot del episodio trae `characterExternalIds` para el grafo pero no escribe la N:M en Postgres, evitando dos escritores compitiendo por la misma fila.
- **Placeholders**: `LocationPersistenceService.findOrCreatePlaceholder` y `EpisodePersistenceService.findOrCreatePlaceholders` con `Propagation.MANDATORY` (solo tienen sentido dentro de la transacción del personaje). El nombre de origen/ubicación viaja en el snapshot para que el placeholder ya tenga `name`.
- **Enums** `CharacterStatus`/`CharacterGender` con `UNKNOWN` y parseo tolerante; almacenados como `varchar(16)` (`@Enumerated(STRING)`), legibles en SQL.
- **Timestamps** vía `@PrePersist/@PreUpdate` (JPA no aplica los `default` de la BD cuando la columna está mapeada).
- `user_favorites` con `id bigserial` + `UNIQUE(user_id, character_id)` en lugar de PK compuesta: misma garantía sin `@EmbeddedId`.
- **`Character` sombrea `java.lang.Character`** dentro de su paquete: anotado en la clase; ningún uso de la clase del JDK en el código.

### Revisión
- ✔ Nombres `snake_case`, tablas en plural ([nomenclatura](../.agents/conventions/nomenclatura.md)); `ddl-auto=validate` en producción, `none` en tests (H2 reporta `timestamp with time zone` con un código JDBC que Hibernate 5.6 no acepta para `Instant`).
- ✔ Consultas derivadas comprobadas nombre a nombre (`findByLocationIdAndPlaceholderFalseOrderByNameAsc`, `findByUserIdAndCharacterId`, …). JPQL con `join c.episodes` para el detalle de episodio.
- Riesgo asumido: si `validate` contra Postgres real discrepa en algún tipo, pasar a `none` y anotarlo aquí (los tests con H2 no lo detectan).

### Verificación
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw -q test -Dtest='*PersistenceServiceTest' && echo TESTS_OK
```
Pegar `Tests run:` y fallos.

Commit propuesto: `feat(db): add flyway schema and idempotent jpa upserts keyed by external id`

## 4. Workflow 07 · Persistencia Neo4j

**Requisito**: [spec/04](../.agents/spec/04-modelo-de-datos.md) (consulta resuelta en el grafo); [references/neo4j.md](../.agents/references/neo4j.md); [ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md).

### Decisiones
- **Una interfaz `GraphRepository`** con una implementación `Neo4jGraphRepository` sobre `Neo4jClient`, en vez de tres `*GraphService`: el consumidor y el servicio de consulta dependen de la abstracción y los tests la sustituyen con un único `@MockBean`. Sin entidades `@Node` ni `save()` de SDN: **Cypher `MERGE` explícito** (la referencia advierte que `save()` con colecciones de relaciones duplica o borra sin querer).
- **Upsert de personaje en una sola sentencia** Cypher: `MERGE` del nodo, borrado de `ORIGIN_FROM`/`LOCATED_IN` previas, `FOREACH (x IN CASE WHEN $originId IS NULL THEN [] ELSE [$originId] END | MERGE …)` como "MERGE condicional" cuando no hay referencia, borrado de `APPEARS_IN` que ya no están en el snapshot y `MERGE` de las actuales. Una sentencia = una transacción auto-commit = atómica; si falla, el mensaje entero se reintenta y el upsert de Postgres es idempotente (la reconciliación es el reintento, ADR-004).
- **Nodos mínimos** (`externalId`, `name`, `code`); los atributos se leen de Postgres al componer la respuesta. Parámetros siempre en `HashMap` (no `Map.of`: rechaza valores `null`).
- **Constraints** únicos por `externalId` con sintaxis 4.4 (`CREATE CONSTRAINT … IF NOT EXISTS ON (n:Label) ASSERT n.externalId IS UNIQUE`) creados por `GraphSchemaInitializer` al arrancar; si Neo4j no responde se registra `ERROR` y la aplicación sigue (el health lo delata; la API de Postgres sigue sirviendo).
- **Consulta obligatoria** `findRelated`: episodios compartidos, `ORDER BY sharedEpisodes DESC, externalId ASC LIMIT $limit`.

### Alternativas descartadas
- `Neo4jTransactionManager` explícito + `@Transactional(transactionManager="neo4jTransactionManager")`: obliga a un bean condicional al perfil de test; la sentencia única ya da atomicidad.
- Seis sentencias encadenadas (más legible): no atómicas entre sí.

### Revisión
- ✔ Cypher parametrizado (`$id`, `$episodeIds`), nunca concatenado.
- Riesgo asumido: el Cypher no se ejecuta en tests (sin Neo4j embebido: `neo4j-harness` sería una dependencia más). Se valida en el arranque real con la consulta del workflow 07.
- Nota: el parámetro del lambda de `mappedBy` se llama `row` y no `record` para evitar el identificador restringido de Java ≥ 14 aunque se compile con `--release 11`.

### Verificación (tras una sync completa)
```bash
cd projects && docker compose exec neo4j cypher-shell -u neo4j -p "$NEO4J_PASSWORD" "MATCH (c:Character {externalId:1})-[:APPEARS_IN]->(e)<-[:APPEARS_IN]-(o) WHERE o<>c RETURN o.name, count(e) AS n ORDER BY n DESC LIMIT 5"
```

Commit propuesto: `feat(graph): persist entity relationships in neo4j with merge-based upserts and related-characters query`

## 5. Workflow 03 · Cliente de la API externa

**Requisito**: [spec/02](../.agents/spec/02-integracion-api-externa.md) (4 puntos); [references/rick-and-morty-api.md](../.agents/references/rick-and-morty-api.md).

### Decisiones
| Punto del requisito | Cómo |
|---|---|
| 1 · Cliente desacoplado | `RickAndMortyClient` en `external/`, solo lo usa `SyncProducerService`. Ningún controlador ni servicio de dominio lo conoce. |
| 2 · Tolerancia a fallos | `RestTemplate` dedicado con `connect-timeout`/`read-timeout` (`ExternalApiConfig`); reintento en bucle con backoff fijo para `HttpServerErrorException` y `ResourceAccessException` (red/timeout); 404 → `ExternalPageNotFoundException`; otro estado → `ExternalServiceException`; cuerpo ilegible → `InvalidExternalPayloadException`. |
| 3 · Transformación | `ExternalSnapshotMapper` produce `CharacterSnapshot`/`EpisodeSnapshot`/`LocationSnapshot` (modelo propio, en `sync/message` por ser el contrato del mensaje). `""` → `null`, referencias sin URL → id `null`, URLs → ids, `Alive/Dead/unknown` → enum, `air_date` texto. Los DTOs `External*` no salen del paquete. |
| 4 · Validación | `ExternalPayloadValidator`: página con `results`; elemento con `id > 0`, `name` no vacío, URLs de relación con id entero. Página inválida → se descarta la página; elemento inválido → se descarta el elemento (el productor decide). |

- Sin `resilience4j`/`spring-retry`: un bucle de N intentos es suficiente y no añade dependencias ([spec/09](../.agents/spec/09-criterios-valoracion.md) punto 7).
- Sin `rootUri` en el `RestTemplate`: el cliente construye la URL completa, lo que hace trivial el matching en `MockRestServiceServer`.
- `ParameterizedTypeReference<ExternalPage<T>>` para deserializar el envoltorio genérico `{info, results}`.

### Revisión
- ✔ Orden de `catch` de específico a general (`NotFound` → `HttpServerErrorException|ResourceAccessException` → `HttpStatusCodeException` → `RestClientException`).
- ✔ `Thread.sleep` restaura la interrupción.
- Caso límite cubierto: página con JSON válido pero sin `results` → el validador la rechaza (test `RickAndMortyClientTest`).

### Verificación
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw -q test -Dtest='RickAndMortyClientTest,ExternalPayloadValidatorTest,ExternalSnapshotMapperTest' && echo TESTS_OK
```

Commit propuesto: `feat(external): add fault-tolerant rick and morty client with validation and domain mapping`

## 6. Workflows 04 y 05 · Productor y consumidor Kafka

**Requisito**: [spec/03](../.agents/spec/03-sincronizacion-kafka.md); [ADR-002](../.agents/decisions/ADR-002-idempotencia-sync.md), [ADR-003](../.agents/decisions/ADR-003-topics-kafka.md), [ADR-006](../.agents/decisions/ADR-006-mensajes-irrecuperables.md); [references/kafka-2.0.1.md](../.agents/references/kafka-2.0.1.md).

### Decisiones del productor
- **Lanzable explícitamente**: `POST /api/admin/sync` → `SyncProducerService.launch()` crea el `sync_run` (409 si ya hay uno `RUNNING`, comprobación `synchronized`) y ejecuta `run(runId)` en el `TaskExecutor` de Boot; el endpoint responde 202 con `runId`. Opcional `sync.on-startup=true` (`SyncOnStartupRunner`).
- `StaleSyncRunCleaner` marca `FAILED` los `RUNNING` huérfanos de un proceso anterior al arrancar: sin esto, un reinicio a mitad de sync dejaría el 409 activo para siempre.
- **Orden locations → episodes → characters** ([ADR-002](../.agents/decisions/ADR-002-idempotencia-sync.md)); paginación hasta `info.next == null`; un mensaje por elemento con clave `externalId`.
- **Política de fallos**: elemento inválido → `WARN`, `skipped++`, continuar; página que falla tras los reintentos del cliente (o página 1 inexistente, o publicación fallida) → `ERROR`, `failedPages++`, pasar a la siguiente entidad; final `COMPLETED`/`PARTIAL`; excepción inesperada → `FAILED`. Los contadores quedan en `sync_runs` y se exponen en `GET /api/admin/sync/{runId}`.
- `SyncPublisher` espera el ack del broker (`send(...).get(30 s)`; en spring-kafka 2.8 `send` devuelve `ListenableFuture`, no `CompletableFuture`) para que los contadores sean exactos y una caída del broker se detecte por página.
- **Mensaje** `SyncMessage {schemaVersion, syncRunId, entityType, externalId, fetchedAt, data}` serializado por `SyncMessageCodec` con el `ObjectMapper` de Boot (soporte `java.time`); `data` es el snapshot propio, nunca el JSON del proveedor ([ADR-003](../.agents/decisions/ADR-003-topics-kafka.md)).
- **Productor idempotente**: se mantiene el default de kafka-clients 3.1 (`enable.idempotence=true`, soportado por broker 2.0.1 vía `InitProducerId`) y `acks=all` fijo (obligatorio con idempotencia; cambiarlo lanzaría `ConfigException`). El plan B de la referencia es una variable: `KAFKA_PRODUCER_IDEMPOTENCE=false`.
- **Topics** declarados por la aplicación (`KafkaAdmin.NewTopics`, 1 partición, RF 1, 3 topics + 3 `.DLT`), con `auto.create.topics.enable=false` en el broker y `admin.fail-fast=true`.

### Decisiones del consumidor
- **Un único `@KafkaListener`** (`SyncConsumer.onMessage`) suscrito a los tres topics, sobre `String`, con despacho por `entityType`; por mensaje: `decode` → `upsert` en Postgres (transacción propia que **commitea**) → `MERGE` en Neo4j → retorno. Un solo hilo consumidor: sin carreras entre el placeholder de una referencia y el upsert real de la misma entidad (hallazgo de la revisión, más abajo). **Sin `@Transactional` en el listener** a propósito: Postgres debe estar commiteado antes de tocar el grafo ([ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md)).
- **Offsets**: `enable-auto-commit=false` + `AckMode.RECORD`: el contenedor commitea solo cuando el listener retorna sin excepción o cuando el recuperador (DLT) ha terminado. Nunca antes de persistir. `MANUAL_IMMEDIATE` habría exigido `Acknowledgment` + `commitRecovered` sin ganancia.
- **Errores** ([ADR-006](../.agents/decisions/ADR-006-mensajes-irrecuperables.md)): `DefaultErrorHandler(CountingDltRecoverer(DeadLetterPublishingRecoverer), FixedBackOff(1000 ms, 2))` → 3 intentos y a `<topic>.DLT`; `InvalidMessageException` (JSON roto, `schemaVersion` desconocido, snapshot inválido, tipo que no coincide con el topic) es **no reintentable** y va al DLT al primer intento. El clasificador recorre las causas, así que la envoltura `ListenerExecutionFailedException` no lo oculta.
- `CountingDltRecoverer` delega en el recuperador de spring-kafka (cabeceras de excepción incluidas), deja la **traza** exigida (`ERROR` con topic/partición/offset/clave/causa) e incrementa `sync_runs.failed_messages` (`@Modifying` atómico, dentro de un servicio `@Transactional`); el `syncRunId` se extrae del payload de forma tolerante y, si no está, se atribuye al último run. Un fallo del contador se registra y **no** vuelve a lanzar: evitaría publicar el DLT dos veces.
- Serializadores/deserializadores `String` explícitos en las factorías: el `KafkaTemplate` que usa el DLT debe serializar lo mismo que el consumidor deserializa.

### Alternativas descartadas
- `@RetryableTopic` / retry topics: infraestructura extra. Transacciones Kafka exactly-once: sobreingeniería con un consumidor y broker 2.0.1. Deduplicar por offset: frágil ante reejecuciones completas ([ADR-002](../.agents/decisions/ADR-002-idempotencia-sync.md)).

### Revisión
- ✘→✔ Dependencia circular detectada en diseño: `SyncRunService` lanzaba el productor y el productor cerraba el run en `SyncRunService`. Resuelto invirtiendo: `SyncProducerService.launch()` orquesta y `SyncRunService` solo gestiona filas.
- ✘→✔ `InvalidMessageException` vivía en `sync/consumer` pero la usa `sync/message` (codec): movida a `sync/message` para que las dependencias vayan en un solo sentido.
- ✔ Firmas verificadas con `javap` en los jars locales: `DefaultErrorHandler(ConsumerRecordRecoverer, BackOff)`, `ConsumerAwareRecordRecoverer.accept(record, consumer, exception)`, `DeadLetterPublishingRecoverer(KafkaOperations)`, `KafkaAdmin.NewTopics(NewTopic...)`, `KafkaTemplate.send → ListenableFuture`.
- Pregunta del revisor "¿y si el mensaje llega antes que sus dependencias?": placeholders en Postgres y `MERGE` en Neo4j; el orden de publicación solo minimiza el caso.

Revisión independiente (subagente en rol revisor, sobre el código completo):
- ✘→✔ **Carrera de placeholders** (grave): tres `@KafkaListener` = tres hilos; un personaje podía crear el placeholder de un episodio mientras otro hilo hacía el upsert real del mismo `external_id` → violación de la restricción única → reintento y, con dos colisiones, un mensaje válido al DLT. Corregido con **un único listener suscrito a los tres topics** (`SyncConsumer.onMessage`, despacho por `entityType`): un solo hilo, sin carreras; ADR-003 ya renuncia al paralelismo (1 partición). Coste: el orden entre topics no está garantizado, lo que los placeholders ya cubren.
- ✘→✔ **Sufijo DLT no cableado**: los topics `.DLT` se creaban con `sync.dlt-suffix` pero el `DeadLetterPublishingRecoverer` usaba su resolutor por defecto (`.DLT` fijo); con otro sufijo el DLT no existiría y, con auto-create desactivado, el mensaje nunca se descartaría. Corregido con un resolutor de destino que usa `SyncProperties.dltOf(topic)` y la misma partición.
- ✘→✔ **`finish` podía pisar `failed_messages`**: el `UPDATE` completo de Hibernate escribía el valor cargado mientras el consumidor lo incrementaba con JPQL atómico. Corregido con `@DynamicUpdate` en `SyncRun` (solo se actualizan las columnas cambiadas).
- ✘→✔ **Run atascado en `RUNNING`** si el propio `finish` falla (Postgres caído): el segundo `finish` del `catch` va ahora protegido con `try/catch` + `ERROR`; la fila la repara `StaleSyncRunCleaner` en el siguiente arranque (limitación documentada).
- ✘→✔ **Propiedades `spring.kafka.listener.*` muertas** con una factoría propia (el configurador de Boot está tipado `<Object,Object>` y no es aplicable sin casts): eliminada la clave del `application.yml` y comentado en `KafkaConsumerConfig` que todo lo del contenedor se fija ahí.
- ✔ Verificado por el revisor: firmas de spring-kafka 2.8.11, SDN 6.3.18, Security 5.7 y `RestTemplateBuilder`; `IN ()` vacío seguro en Hibernate 5.6; DDL compatible PG10/H2 y coherente con `ddl-auto=validate`; sin ciclos de beans; orden de publicación, placeholders, DLT y códigos de estado conformes a ADR-001..006 y `api-rest.md`.

### Verificación
```bash
curl -s -X POST http://localhost:4080/api/admin/sync -H "Authorization: Bearer $TOKEN"
```
```bash
cd projects && docker compose exec postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select (select count(*) from characters) c, (select count(*) from episodes) e, (select count(*) from locations) l, (select count(*) from character_episodes) ce;"
```
```bash
cd projects && docker compose exec neo4j cypher-shell -u neo4j -p "$NEO4J_PASSWORD" "MATCH (n) RETURN labels(n)[0] AS label, count(*) AS n"
```
Esperado: 826 / 51 / 126 (y los mismos conteos tras una segunda sync). Pegar ambas tablas y las líneas de log con `sync run`.

Commits propuestos: `feat(sync): publish external snapshots to kafka topics via admin-triggered sync run` · `feat(sync): consume kafka topics with idempotent upserts into postgres and neo4j and dead-letter handling`

## 7. Workflow 08 · Autenticación

**Requisito**: [spec/05](../.agents/spec/05-api-propia.md) punto 2; bonus B1; [ADR-005](../.agents/decisions/ADR-005-autenticacion.md).

### Decisiones
- `TokenService`: JWT compacto `header.payload.signature` con `javax.crypto.Mac` HmacSHA256, Base64 URL sin padding, claims `sub`, `uid`, `iat`, `exp`. Validación: exactamente 3 partes no vacías, Base64 decodificable, **firma comparada en tiempo constante** (`MessageDigest.isEqual`), `alg == HS256`, `exp` futuro. Un `Mac` nuevo por llamada (no es thread-safe). `Clock` inyectable (constructor package-private) para probar la caducidad sin esperar.
- `BearerTokenFilter` (`OncePerRequestFilter`) se instancia con `new` dentro de `SecurityConfig`: si fuera un bean, Boot lo registraría además como filtro servlet fuera de la cadena de seguridad (se ejecutaría dos veces). Sin cabecera → continúa anónimo (rutas públicas); cabecera inválida → escribe el `ApiError` 401 y corta (está fuera del alcance del `ControllerAdvice`).
- `SecurityConfig` con `SecurityFilterChain` (Spring Security 5.7; `WebSecurityConfigurerAdapter` está deprecado): stateless, CSRF off, CORS para los orígenes del SPA (`app.cors.allowed-origins`), `BCryptPasswordEncoder`. Protegidas `/api/users/me/**` y `/api/admin/**`; **el resto `permitAll`**: los datos sincronizados son públicos por ADR-005 y así una ruta inexistente devuelve 404 `NOT_FOUND` como exige [formato-error](../.agents/conventions/formato-error.md) (con deny-by-default un anónimo recibiría 401 en cualquier ruta desconocida).
- `AuthService`: registro con `username` normalizado (trim + minúsculas), 409 si existe, BCrypt; login → 401 con mensaje genérico. `CurrentUser.get()` lee el `SecurityContext` sin depender de resolutores de argumentos (más simple de probar en `@WebMvcTest`).
- Validación de entrada: `username` 3–64 `[A-Za-z0-9._-]`, `password` 8–72 (límite de BCrypt).

### Revisión
- ✔ Sin `jjwt`/`nimbus`; cero dependencias nuevas.
- ✔ Casos límite en `TokenServiceTest`: firma alterada, payload alterado, caducado, otro secreto, formatos rotos.
- Limitaciones documentadas: sin roles, sin refresh, sin revocación ([ADR-005](../.agents/decisions/ADR-005-autenticacion.md)).
- ✘→✔ (revisión independiente) `AuthService.login` reutilizaba `InvalidTokenException` para "credenciales incorrectas": mismo 401 pero nombre engañoso. Nueva `common/UnauthorizedException` (credenciales, o ausencia de usuario en `CurrentUser`), mapeada a 401 junto a `InvalidTokenException`.
- Decisión defendida ante el revisor: un token **caducado o manipulado devuelve 401 incluso en rutas públicas** (`BearerTokenFilter` corta la cadena). Es lo que pide ADR-005 ("cualquier fallo → 401") y lo que necesita el SPA para cerrar sesión de forma centralizada; la alternativa (ignorar la cabecera inválida y seguir anónimo) ocultaría sesiones caducadas hasta la primera ruta protegida.

### Verificación
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw -q test -Dtest='TokenServiceTest,AuthControllerTest' && echo TESTS_OK
```

Commit propuesto: `feat(auth): add user registration, login and self-issued hmac tokens with stateless security filter`

## 8. Workflow 09 · Favoritos

**Requisito**: [spec/05](../.agents/spec/05-api-propia.md) punto 3.

### Decisiones
- Entidad explícita `Favorite` (`user_favorites`) con `created_at` para listar en orden de inserción.
- `POST /api/users/me/favorites/{characterId}` **idempotente**: 201 al crear, **200** con el mismo cuerpo si ya era favorito (decisión registrada en [api-rest.md](../.agents/conventions/api-rest.md)). Razón: el cliente puede reintentar sin tratar un 409 como error; el estado final es el mismo. 404 si el personaje no existe o es placeholder. `DELETE` → 204, 404 si no era favorito.
- La respuesta es `CharacterSummary` (misma forma que el listado): el frontend reutiliza la tarjeta.

### Revisión
- ✘→✔ (ver §2) captura de `DataIntegrityViolationException` dentro de la transacción eliminada; el duplicado concurrente responde 409 vía handler.
- ✔ Controlador no toca repositorios; usuario desde `CurrentUser`.

### Verificación
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw -q test -Dtest='Favorite*Test' && echo TESTS_OK
```

Commit propuesto: `feat(api): manage per-user favorite characters`

## 9. Workflow 10 · API de consulta

**Requisito**: [spec/05](../.agents/spec/05-api-propia.md) puntos 4 y 5; [api-rest.md](../.agents/conventions/api-rest.md).

### Decisiones
- **Paginación con parámetros explícitos** `page`/`size` validados con `@Min/@Max(100)` y `@Validated` en el controlador → `ConstraintViolationException` → 400 con `details[{field:"size"}]`. No se usa `Pageable`/`@ParameterObject`: springdoc 1.x lo documenta correctamente solo con el módulo `springdoc-openapi-data-rest` (una dependencia más) y el contrato pedido es `page`/`size` sin `sort`.
- **Filtros** con `Specification` (`CharacterSpecifications`): `name` parcial case-insensitive, `species` exacta case-insensitive, `status`/`gender` enum. Conversión enum case-insensitive (`WebConfig`): `?status=alive` vale; `?status=banana` → `MethodArgumentTypeMismatchException` → 400 con el campo.
- Listados y detalle **excluyen placeholders** (`placeholder=false`): una entidad solo referenciada aún no tiene atributos; exponerla sería mostrar filas vacías. Detalle de placeholder → 404 (desfase pendiente hasta que llegue su snapshot).
- **Relacionados**: id interno → `externalId` → Cypher (orden y `sharedEpisodes`) → `findByExternalIdIn` en una consulta → composición conservando el orden del grafo; ids sin fila visible en Postgres se omiten con `WARN` ([ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md)).
- Mapeo entidad → DTO en mapeadores estáticos dentro de servicios `@Transactional(readOnly = true)` (relaciones LAZY con `open-in-view=false`). Episodios del detalle ordenados por `code`.
- Residentes de una localización = inverso de `location_id` en Postgres (no hace falta el grafo).

### Revisión
- ✔ `id` en rutas = id interno, respuestas con `externalId` ([ADR-001](../.agents/decisions/ADR-001-identificador-externo.md)).
- ✔ Página fuera de rango → 200 con `content` vacío (Spring Data), cubierto en `CharacterControllerTest`.
- Nota de rendimiento: `lower(name) LIKE '%x%'` sin índice funcional (decisión de §3); aceptable para el volumen.
- ✘→✔ (revisión independiente) código muerto eliminado: `Character.placeholder(...)` (ningún flujo crea placeholders de personaje: el snapshot de episodio no escribe la N:M) y `CharacterFilter.none()`. La columna `characters.placeholder` se conserva como filtro defensivo y así se anota en la entidad.

### Verificación
```bash
curl -s "http://localhost:4080/api/characters?name=rick&page=0&size=5"
```
```bash
curl -s "http://localhost:4080/api/characters/1/related?limit=3"
```

Commit propuesto: `feat(api): expose paginated character search, detail and graph-backed related characters`

## 10. Workflow 11 · OpenAPI y Swagger activable

**Requisito**: [spec/05](../.agents/spec/05-api-propia.md) punto 1; petición del candidato: Swagger no obligatorio en producción → [ADR-009](../.agents/decisions/ADR-009-openapi-toggle.md).

### Decisiones
- springdoc-openapi-ui 1.8.0 (rama 1.x = Boot 2.x/javax). Bean `OpenAPI` con `info` y esquema `bearerAuth`; `@Tag` por controlador, `@Operation` + `@ApiResponse` con todos los códigos de [api-rest.md](../.agents/conventions/api-rest.md), `@Parameter` en filtros y paginación, `@Schema` en DTOs, `@SecurityRequirement` en `Favorites` y `Admin`. `ApiError` documentado como esquema en cada respuesta de error.
- **Interruptor** `SWAGGER_ENABLED` → `springdoc.api-docs.enabled` y `springdoc.swagger-ui.enabled`; `OpenApiConfig` condicionada a la misma propiedad. Con `false`: 404 en `/v3/api-docs` y `/swagger-ui.html`, sin bean `OpenAPI`. Variable en `.env` y en el servicio `backend` del compose.

### Revisión
- ✔ Cada fila de [api-rest.md](../.agents/conventions/api-rest.md) tiene su endpoint anotado con los mismos códigos (auth 201/400/409 y 200/400/401; characters 200/400/404; related 200/400/404/503; favorites 200/201/401/404/204; admin 202/401/409 y 200/401/404).
- Riesgo asumido: la exactitud del JSON generado solo se comprueba con la aplicación arrancada (comando abajo).

### Verificación
```bash
curl -s http://localhost:4080/v3/api-docs | python3 -c "import json,sys; d=json.load(sys.stdin); [print(m.upper(), p, sorted(o['responses'])) for p,ops in d['paths'].items() for m,o in ops.items()]"
```
```bash
cd projects && SWAGGER_ENABLED=false docker compose up -d --force-recreate backend && sleep 40 && curl -s -o /dev/null -w '%{http_code}\n' http://localhost:4080/v3/api-docs
```
Esperado: la lista de rutas con sus códigos; y `404` con el interruptor apagado (volver a `true` después).

Commit propuesto: `docs(api): document endpoints, parameters and error responses with springdoc`

## 11. Workflow 16 · Pruebas backend

**Requisito**: [spec/07](../.agents/spec/07-pruebas.md) puntos 1–3; [references/testing-backend.md](../.agents/references/testing-backend.md); skill [testing-aislado](../.agents/skills/testing-aislado/SKILL.md).

### Tabla de casos (nivel más bajo que prueba el comportamiento)
| Clase | Nivel | Casos, incluidos los límite |
|---|---|---|
| `RickAndMortyClientTest` | `MockRestServiceServer` | 200 parseado; 404 → fin de paginación; 500 y luego 200 (reintento); 5xx × N → `ExternalServiceException`; `SocketTimeoutException` reintentado; 403 → `ExternalServiceException`; HTML en vez de JSON → `InvalidExternalPayloadException`; JSON sin `results` → rechazado por el validador |
| `ExternalPayloadValidatorTest` | unitario | sin `results`; id ≤ 0; nombre vacío; URL de episodio no numérica; origen `unknown` aceptado |
| `ExternalSnapshotMapperTest` | unitario | `""` → `null`; `origin.url == ""` → `null`; ids de URLs (sin duplicados); estado no reconocido → `UNKNOWN`; `air_date` texto; código desde `episode` |
| `SyncMessageCodecTest` | unitario | roundtrip con `Instant`; `schemaVersion` 99; JSON roto; sin `data`; tipo de entidad distinto; `tryExtractRunId` tolerante |
| `SyncProducerServiceTest` | unitario (cliente/publicador/run mockeados) | orden locations→episodes→characters y todas las páginas; página fallida → `PARTIAL` y siguiente entidad; elemento inválido → `skipped`; publicación fallida → página fallida; error inesperado → `FAILED` |
| `SyncRunServiceTest` | unitario | 409 si `RUNNING`; creación; atribución del fallo al último run o al indicado; sin run → solo aviso; 404 |
| `AuthServiceTest` | unitario | normalización (trim + minúsculas) y hash; usuario duplicado → 409; contraseña incorrecta y usuario inexistente → mismo 401 |
| `SyncAdminControllerTest` | `@WebMvcTest` | 401 sin token; 202 con `runId`; 409 sync en curso; estado y contadores; 404 |
| `CharacterQueryServiceTest` | `@DataJpaTest` H2 + Flyway | filtros reales en SQL: nombre parcial case-insensitive, combinación status/species/gender, página más allá del final con totales, detalle con origen/ubicación/episodios ordenados por código, placeholders ocultos |
| `TokenServiceTest` | unitario | roundtrip; firma y payload alterados; caducado (reloj fijo); otro secreto; formatos rotos |
| `AuthControllerTest` | `@WebMvcTest` | 201; 409 `ApiError`; 400 con `details`; JSON malformado; 200 con token; 401 |
| `FavoriteControllerTest` | `@WebMvcTest` (+ filtro real) | 401 sin token con `ApiError`; token manipulado; listado del usuario del token; 201 y 200 repetido; 404; 204 y 404 |
| `FavoriteServiceTest` | unitario | crear / ya existente; personaje inexistente; eliminar inexistente; listado |
| `CharacterControllerTest` | `@WebMvcTest` | `size=101` → 400 `details[size]`; enum inválido → 400; enum en minúsculas OK; página fuera de rango → 200 vacío; 404 `ApiError`; `limit=0` → 400; ruta no mapeada → 404; método no soportado → 405; id no numérico → 400; Neo4j caído → 503; violación de integridad → 409; excepción inesperada → 500 sin detalles internos |
| `CharacterPersistenceServiceTest` (+ `Episode*`, `Location*`) | `@DataJpaTest` H2 + Flyway | placeholders creados; upsert ×2 sin duplicar (filas y N:M); reemplazo del conjunto y actualización de atributos; origen `null`; placeholder completado conservando id |
| `RelatedCharactersServiceTest` | unitario | orden del grafo conservado; `sharedEpisodes`; id sin fila o placeholder omitido; lista vacía sin consultar Postgres; 404 |
| **`SyncFlowIT`** | `@SpringBootTest` + `@EmbeddedKafka` + H2 + `@MockBean GraphRepository` | publicar → fila + placeholders; mismo mensaje ×2 → mismos conteos; `{not-json` → `rm.characters.DLT` con cabeceras de excepción y el siguiente mensaje válido se procesa; `schemaVersion 99` → DLT y `failed_messages ≥ 1` en su run; mensajes de `rm.locations` y `rm.episodes` persistidos por el mismo listener |

### Decisiones de diseño de las pruebas
- **Sin red ni Docker**: H2 `MODE=PostgreSQL` con las migraciones reales (`@AutoConfigureTestDatabase(replace = NONE)`, imprescindible: `@DataJpaTest` sustituiría el datasource por un H2 sin modo Postgres), Kafka embebido (`spring-kafka-test` trae `kafka_2.13` y su test-jar, verificado en el POM del artefacto), Neo4j excluido de la autoconfiguración en el perfil `test` y sustituido por `@MockBean GraphRepository` (reemplaza la definición del bean antes de instanciarlo, así el constructor real nunca pide un `Neo4jClient`).
- **Espera sin `Awaitility` ni `Thread.sleep`** en `SyncFlowIT`: `verify(graphRepository, timeout(20 s))` de Mockito sondea hasta que el consumidor llama al grafo, y como esa llamada ocurre después del commit de Postgres, también verifica el orden de [ADR-004](../.agents/decisions/ADR-004-consistencia-postgres-neo4j.md). Para el contador del DLT, `TestAwait.until` (sondeo acotado propio).
- Ids externos distintos por test: el contenedor de listeners es compartido entre tests de la misma clase.
- Consumidor de test del DLT con `auto.offset.reset=latest` creado **antes** de publicar: solo ve los registros de su test aunque otros hayan llenado el DLT.
- `TestData` con fábricas mínimas; ningún test de getters; ningún `@SpringBootTest` donde basta un slice.

### Revisión
- ✔ Cada caso límite obligatorio de [testing-aislado](../.agents/skills/testing-aislado/SKILL.md) tiene una fila en la tabla.
- ✔ (revisión independiente, verificado en los jars) el slice `@WebMvcTest` aplica `throw-exception-if-no-handler-found` y `add-mappings=false` (`MockMvcDispatcherServletCustomizer`), incluye `@ControllerAdvice`, `WebMvcConfigurer` y `MethodValidationPostProcessor`; `@DataJpaTest` incluye Flyway; H2 2.1.214 acepta `bigserial` y Hibernate 5.6 inserta identidades con `default`.

Revisión independiente de los tests (subagente en rol QA/revisor):
- ✘→✔ **Test rojo determinista**: `Jackson2ObjectMapperBuilder.json().build()` no desactiva `WRITE_DATES_AS_TIMESTAMPS` (eso lo hace la autoconfiguración de Boot), así que `fetchedAt` se serializaba como número y la aserción ISO fallaba. Ahora `TestData.objectMapper()` desactiva esa feature y lo usan el codec, el token y el productor.
- ✘→✔ **Test rojo determinista**: `SyncRunServiceTest` creaba un `SyncRun` sin `id` y `recordFailedMessage` lo descartaba (id nulo). Id fijado por reflexión; añadido el caso "sin ningún run → solo aviso".
- ✘→✔ **Dependencia del orden**: una única H2 con nombre (`DB_CLOSE_DELAY=-1`) se compartía entre los contextos cacheados; las filas commiteadas por `SyncFlowIT` rompían los `count()` absolutos de los `@DataJpaTest` si corrían después. URL con `${random.uuid}`: una base por contexto.
- ✘→✔ **Inestabilidad**: el contador `failed_messages == 1` podía ser 2 si el DLT sin `runId` de otro test se atribuía al mismo run; ahora `≥ 1`.
- ✘→✔ `application-test.yml` estaba en `src/main/resources` (viajaba en el jar): movido a `src/test/resources`.
- ✘→✔ Un test = un comportamiento: casos 201/200 y 204/404 de favoritos separados; tokens malformados y payloads sin `runId` como `@ParameterizedTest`; test de placeholder de localización movido a su clase.
- ✘→✔ Huecos de cobertura cerrados: `AuthServiceTest`, `SyncAdminControllerTest`, `CharacterQueryServiceTest` (filtros en SQL real), 503/409/500 del handler global, listeners de locations/episodes en `SyncFlowIT`.
- Desviación consciente: `TestAwait` sondea con `Thread.sleep(100)` acotado (10 s) para esperar el commit del contador. La skill prohíbe los sleeps fijos; un sondeo acotado es exactamente lo que hace Awaitility, que no se añade por economía de dependencias. `verify(timeout)` de Mockito no vale aquí porque se satisface al invocar el método, antes del commit de la transacción.

### Verificación
```bash
cd projects/backend && JAVA_HOME=$(/usr/libexec/java_home -v 11) ./mvnw test 2>&1 | grep -E "Tests run:|FAIL|ERROR\]" | tail -25
```
Esperado: todas las líneas `Tests run: … Failures: 0, Errors: 0` y `BUILD SUCCESS`. Pegar solo esas líneas y, si algo falla, las 30 líneas anteriores al primer `FAIL`.

Commit propuesto: `test(backend): cover external client, sync flow with embedded kafka, auth and api edge cases`

## 12. Decisiones transversales

| Tema | Decisión | Por qué |
|---|---|---|
| Versiones | Boot 2.7.18, springdoc 1.8.0, resto por BOM; imágenes según [ADR-008](../.agents/decisions/ADR-008-imagenes-docker.md) | Coherencia con JDK 11 y broker 2.0.1 ([stack-versiones](../.agents/references/stack-versiones.md)) |
| Compilación | `maven.compiler.release=11` | El host tiene JDK 17 por defecto; `--release` rechaza APIs > 11 en compilación, no en el build Docker |
| Transacciones | JPA `@Primary`; Neo4j auto-commit por sentencia | Doble autoconfiguración de `transactionManager` (§2); sin transacción distribuida (ADR-004) |
| Rutas públicas | `permitAll` salvo `/api/users/me/**` y `/api/admin/**` | Datos sincronizados públicos (ADR-005); 404 en rutas desconocidas (formato-error) |
| Favoritos repetidos | 200 idempotente | Reintentos del cliente sin tratar 409 como error |
| Rutas no mapeadas / métodos | 404 y 405 con `ApiError` | Formato homogéneo hasta en los errores del framework |
| Kafka producer | `acks=all` + idempotencia por defecto, `KAFKA_PRODUCER_IDEMPOTENCE` como plan B | Broker 2.0.1 soporta `InitProducerId`; cambiar `acks` con idempotencia activa rompe el arranque |
| Offsets | `AckMode.RECORD`, auto-commit off | Commit solo tras persistir o tras publicar en DLT |
| Swagger | `SWAGGER_ENABLED` | [ADR-009](../.agents/decisions/ADR-009-openapi-toggle.md) |
| Dependencias añadidas | ninguna fuera de [stack-versiones](../.agents/references/stack-versiones.md) | Sin resilience4j, spring-retry, Lombok, MapStruct, jjwt, Awaitility, Testcontainers |

## 13. Limitaciones conocidas y qué se haría con más tiempo
- **Kafka desde el host**: el compose anuncia `EXTERNAL://localhost:9092` pero publica `4093:9092`; un backend fuera de Docker se conecta y es redirigido a 9092. No se ha tocado el compose por decisión del candidato; la corrección es una línea (`KAFKA_ADVERTISED_LISTENERS: …,EXTERNAL://localhost:4093`). Dentro del compose no afecta.
- El `README.md` documenta los puertos reales del compose (4080, 4300, 4575/4788, 4093), que no coincidían con la versión anterior del README.
- Sin roles ni refresh token; `POST /api/admin/sync` abierto a cualquier usuario autenticado (ADR-005).
- Sin reproceso del DLT (se inspecciona con las herramientas de consola de Kafka).
- Neo4j no se ejecuta en tests (el Cypher se valida en el arranque real). Con más tiempo: `neo4j-harness` 4.4 o Testcontainers como perfil opcional.
- Bonus no abordados por decisión: B2 (`raw_payloads` con el JSON crudo) y el registro `processed_messages` de B3; ambos encajan en el diseño actual (tabla nueva + upsert por `(entity_type, external_id)` / inserción en la transacción del upsert).
- Índice funcional `lower(name)` descartado por compatibilidad con H2; en Postgres real bastaría una migración `V5` con `CREATE INDEX … ON characters (lower(name))` protegida por perfil.
