# decisions/ — Architecture Decision Records

Cada decisión de diseño relevante (y cada dependencia añadida) tiene un ADR corto. Son la base del apartado "decisiones" del README de entrega y de la entrevista ([spec/10-entrega.md](../spec/10-entrega.md)).

Estados: `Propuesta` (redactada por el agente, pendiente de confirmación humana) · `Aceptada` · `Reemplazada por ADR-xxx`.

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-001](ADR-001-identificador-externo.md) | Identificador externo, id interno y campos ausentes | Propuesta |
| [ADR-002](ADR-002-idempotencia-sync.md) | Idempotencia extremo a extremo de la sincronización | Propuesta |
| [ADR-003](ADR-003-topics-kafka.md) | Topics, claves y formato de mensaje | Propuesta |
| [ADR-004](ADR-004-consistencia-postgres-neo4j.md) | Coherencia entre PostgreSQL y Neo4j ante fallos parciales | Propuesta |
| [ADR-005](ADR-005-autenticacion.md) | Autenticación con token propio (bonus B1) | Propuesta |
| [ADR-006](ADR-006-mensajes-irrecuperables.md) | Dead Letter Topic y registro de mensajes procesados (bonus B3) | Propuesta |
| [ADR-007](ADR-007-best-practices-angular.md) | Adopción de las buenas prácticas oficiales de Angular y reglas por versión | Propuesta |
| [ADR-008](ADR-008-imagenes-docker.md) | Imágenes Docker multi-arch, healthchecks y límites de recursos | Propuesta |

Plantilla: [TEMPLATE.md](TEMPLATE.md). Numerar consecutivamente. Un ADR nuevo por cada dependencia externa no listada en [references/stack-versiones.md](../references/stack-versiones.md).
