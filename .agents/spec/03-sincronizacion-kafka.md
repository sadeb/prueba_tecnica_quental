# Requisito 2 · Sincronización a través de la cola de mensajes

1. Proceso de sincronización **lanzable explícitamente** (comando de arranque, tarea o endpoint de administración) que recorra la fuente externa y publique su contenido en Kafka.
2. **Separación productor / consumidor**: productor = descarga y publicación; consumidor = transformación y persistencia. La escritura en BD no depende del ritmo ni disponibilidad de la fuente externa.
3. **Idempotencia extremo a extremo**: reejecutar la sync o reprocesar un mensaje ya consumido no duplica registros ni corrompe relaciones. Kafka entrega **al menos una vez** por defecto.
4. **Paginación** de la fuente y comportamiento controlado ante **fallos parciales**: un mensaje defectuoso no bloquea indefinidamente el consumo ni deja el conjunto a medias sin traza.
5. **Diseño justificado** de topics, claves de mensaje y formato de carga útil.

Relacionado:
- [decisions/ADR-002-idempotencia-sync.md](../decisions/ADR-002-idempotencia-sync.md)
- [decisions/ADR-003-topics-kafka.md](../decisions/ADR-003-topics-kafka.md)
- [decisions/ADR-006-mensajes-irrecuperables.md](../decisions/ADR-006-mensajes-irrecuperables.md)
- [references/kafka-2.0.1.md](../references/kafka-2.0.1.md)
- Workflows: [04-productor-kafka.md](../workflows/04-productor-kafka.md), [05-consumidor-kafka.md](../workflows/05-consumidor-kafka.md)
