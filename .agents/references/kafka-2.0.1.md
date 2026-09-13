# Kafka 2.12-2.0.1 (broker) con spring-kafka 2.8

## Compatibilidad
- `kafka-clients` 3.1 (gestionado por Boot 2.7.18) habla con brokers ≥ 0.10 gracias a la negociación de versión de API. Broker 2.0.1 es válido.
- **Idempotencia del productor** viene activada por defecto en clients ≥ 3.0 y requiere broker ≥ 0.11: OK. Si el broker rechaza (`ClusterAuthorizationException` / `UnsupportedVersionException`), fijar `spring.kafka.producer.properties.enable.idempotence=false` y `acks=all`. Registrar el resultado en [ADR-003](../decisions/ADR-003-topics-kafka.md).
- Alternativa si aparecen problemas: sobrescribir `<kafka.version>2.8.2</kafka.version>` en el POM (spring-kafka 2.8 requiere clients ≥ 3.0, así que preferir la primera opción).

## Imágenes Docker
- `zookeeper:3.4.13` (oficial, multi-arch; es la versión de ZK que empaqueta Kafka 2.0.1) + `wurstmeister/kafka:2.12-2.0.1` (tag idéntico al enunciado, multi-arch). Descartadas por ser solo amd64: `confluentinc/cp-kafka:5.0.1`/`cp-zookeeper:5.0.1`, `zookeeper:3.4.14`, `wurstmeister/zookeeper` ([ADR-008](../decisions/ADR-008-imagenes-docker.md)).
- `wurstmeister/kafka`: cualquier `KAFKA_FOO_BAR` se convierte en `foo.bar` de `server.properties` (excepto `KAFKA_HEAP_OPTS`, `KAFKA_JVM_PERFORMANCE_OPTS`, `KAFKA_OPTS`). Obligatorios: `KAFKA_ZOOKEEPER_CONNECT` y `KAFKA_LISTENERS`. Scripts en `/opt/kafka/bin` (en `PATH`). Con `KAFKA_LISTENERS` definido no necesita el socket de Docker.
- Listeners: `INTERNAL://kafka:29092` para contenedores, `EXTERNAL://localhost:9092` para el host. `KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL`.
- Healthcheck: `kafka-broker-api-versions.sh --bootstrap-server 127.0.0.1:29092`. En 2.0.1 `kafka-topics.sh` solo admite `--zookeeper` (no `--bootstrap-server`, que llega en 2.2) y por tanto **no comprueba el broker**. La herramienta lanza una JVM: fijar `KAFKA_HEAP_OPTS='-Xmx48m'` en el comando porque el healthcheck hereda el heap del servicio.
- Un solo broker: `offsets.topic.replication.factor=1`, `transaction.state.log.replication.factor=1`, `transaction.state.log.min.isr=1`. `auto.create.topics.enable=false` (topics y `.DLT` vía `NewTopic`; `spring.kafka.admin.fail-fast=true`). `log.dirs=/kafka/kafka-logs` fijo (el default de la imagen lleva el hostname del contenedor).

## spring-kafka 2.8 (lo mínimo)
- Topics creados por la app con `NewTopic` beans (`KafkaAdmin` auto) → 1 partición, RF 1. Sin `auto.create.topics` implícito.
- Productor: `KafkaTemplate<String, String>` con JSON serializado por la app (Jackson) → control del formato.
- Consumidor: `@KafkaListener(topics=..., groupId=...)`, `spring.kafka.consumer.auto-offset-reset=earliest`, `enable-auto-commit=false` con ack `RECORD` o `BATCH` por defecto del contenedor.
- Errores: `DefaultErrorHandler(DeadLetterPublishingRecoverer, FixedBackOff(1000L, 2))` → tras 3 intentos publica en `<topic>.DLT` y sigue. Ver [ADR-006](../decisions/ADR-006-mensajes-irrecuperables.md).
- Deserialización defensiva: `ErrorHandlingDeserializer` o consumir `String` y parsear con try/catch para que un JSON roto vaya al DLT y no bloquee.

## Tests sin infraestructura
`@EmbeddedKafka` de `spring-kafka-test` (broker en memoria, versión = kafka-clients). Ver [testing-backend.md](testing-backend.md).

Relacionado: [spec/03-sincronizacion-kafka.md](../spec/03-sincronizacion-kafka.md), [ADR-003](../decisions/ADR-003-topics-kafka.md).
