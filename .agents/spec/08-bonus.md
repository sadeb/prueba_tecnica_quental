# Mejoras opcionales valorables (BONUS)

No obligatorias. Cuentan como criterio adicional **solo si están bien resueltas y justificadas**.

| # | Bonus | Dónde se decide / implementa |
|---|---|---|
| B1 | Autenticación propia basada en tokens, sin paquetes de terceros para emisión y validación | [ADR-005](../decisions/ADR-005-autenticacion.md) · [wf 08](../workflows/08-autenticacion.md) |
| B2 | Persistir el JSON crudo de la fuente antes de procesar, para reproceso sin volver a descargar | [ADR-002](../decisions/ADR-002-idempotencia-sync.md) · [wf 04](../workflows/04-productor-kafka.md) |
| B3 | Tratamiento explícito de mensajes irrecuperables (cola de descarte) o registro de mensajes procesados | [ADR-006](../decisions/ADR-006-mensajes-irrecuperables.md) · [wf 05](../workflows/05-consumidor-kafka.md) |
| B4 | Versionado del esquema relacional mediante migraciones | [references/postgresql-10.md](../references/postgresql-10.md) · [wf 06](../workflows/06-persistencia-postgres.md) |
| B5 | Cobertura de pruebas más allá de la exigida (backend o frontend) | [07-pruebas.md](07-pruebas.md) |
| B6 | Cualquier decisión de diseño que aporte valor y se sepa fundamentar | [decisions/](../decisions/README.md) |

Regla: un bonus nunca se aborda antes de completar lo obligatorio ([prioridades.md](prioridades.md)).
