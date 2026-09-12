# Kafka 2.12-2.0.1 (broker) con spring-kafka 2.8

## Compatibilidad
- `kafka-clients` 3.1 (gestionado por Boot 2.7.18) habla con brokers ≥ 0.10 gracias a la negociación de versión de API. Broker 2.0.1 es válido.
- **Idempotencia del productor** viene activada por defecto en clients ≥ 3.0 y requiere broker ≥ 0.11: OK. Si el broker rechaza (`ClusterAuthorizationException` / `UnsupportedVersionException`), fijar `spring.kafka.producer.properties.enable.idempotence=false` y `acks=all`. Registrar el resultado en [ADR-003](../decisions/ADR-003-topics-kafka.md).
- Alternativa si aparecen problemas: sobrescribir `<kafka.version>2.8.2</kafka.version>` en el POM (spring-kafka 2.8 requiere clients ≥ 3.0, así que preferir la primera opción).

## Imágenes Docker
- `confluentinc/cp-zookeeper:5.0.1` + `confluentinc/cp-kafka:5.0.1` (Confluent 5.0.x = Apache Kafka 2.0.x). Variables: `KAFKA_ZOOKEEPER_CONNECT`, `KAFKA_ADVERTISED_LISTENERS`, `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`, `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1`.
- O `wurstmeister/zookeeper` + `wurstmeister/kafka:2.12-2.0.1` (tag idéntico al enunciado; `KAFKA_CREATE_TOPICS` opcional).
- Listeners: `INTERNAL://kafka:29092` para contenedores, `EXTERNAL://localhost:9092` para el host.

## spring-kafka 2.8 (lo mínimo)
- Topics creados por la app con `NewTopic` beans (`KafkaAdmin` auto) → 1 partición, RF 1. Sin `auto.create.topics` implícito.
- Productor: `KafkaTemplate<String, String>` con JSON serializado por la app (Jackson) → control del formato.
- Consumidor: `@KafkaListener(topics=..., groupId=...)`, `spring.kafka.consumer.auto-offset-reset=earliest`, `enable-auto-commit=false` con ack `RECORD` o `BATCH` por defecto del contenedor.
- Errores: `DefaultErrorHandler(DeadLetterPublishingRecoverer, FixedBackOff(1000L, 2))` → tras 3 intentos publica en `<topic>.DLT` y sigue. Ver [ADR-006](../decisions/ADR-006-mensajes-irrecuperables.md).
- Deserialización defensiva: `ErrorHandlingDeserializer` o consumir `String` y parsear con try/catch para que un JSON roto vaya al DLT y no bloquee.

## Tests sin infraestructura
`@EmbeddedKafka` de `spring-kafka-test` (broker en memoria, versión = kafka-clients). Ver [testing-backend.md](testing-backend.md).

Relacionado: [spec/03-sincronizacion-kafka.md](../spec/03-sincronizacion-kafka.md), [ADR-003](../decisions/ADR-003-topics-kafka.md).
