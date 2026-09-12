# Glosario

- **Fuente externa**: API pública Rick and Morty (`https://rickandmortyapi.com/api`). Ver [references/rick-and-morty-api.md](references/rick-and-morty-api.md).
- **API propia**: API REST del backend Spring que consume el frontend. Ver [spec/05-api-propia.md](spec/05-api-propia.md).
- **Sincronización (sync)**: proceso explícito que recorre la fuente externa, publica en Kafka y persiste. Ver [spec/03-sincronizacion-kafka.md](spec/03-sincronizacion-kafka.md).
- **Productor**: componente que descarga páginas de la fuente y publica mensajes en Kafka.
- **Consumidor**: componente que lee mensajes de Kafka, transforma y persiste en PostgreSQL y Neo4j.
- **Idempotencia extremo a extremo**: reejecutar la sync o reprocesar un mensaje no duplica ni corrompe. Ver [decisions/ADR-002-idempotencia-sync.md](decisions/ADR-002-idempotencia-sync.md).
- **external_id**: identificador numérico que asigna la fuente externa a cada entidad. Ver [decisions/ADR-001-identificador-externo.md](decisions/ADR-001-identificador-externo.md).
- **Persistencia políglota**: PostgreSQL = fuente de verdad de atributos y usuarios; Neo4j = grafo de relaciones. Ver [spec/04-modelo-de-datos.md](spec/04-modelo-de-datos.md).
- **DLT (Dead Letter Topic)**: topic de descarte para mensajes irrecuperables. Ver [decisions/ADR-006-mensajes-irrecuperables.md](decisions/ADR-006-mensajes-irrecuperables.md).
- **At-least-once**: garantía de entrega de Kafka por defecto; un mensaje puede llegar más de una vez.
- **Placeholder**: nodo/fila creado solo con `external_id` cuando se referencia una entidad aún no sincronizada. Ver [decisions/ADR-004-consistencia-postgres-neo4j.md](decisions/ADR-004-consistencia-postgres-neo4j.md).
- **Componente de presentación / contenedor**: en Angular, componente sin lógica de datos (solo inputs/outputs) vs. componente que orquesta servicios. Ver [conventions/angular.md](conventions/angular.md).
- **Signal**: primitiva reactiva de Angular para el estado (`signal`, `computed`, `linkedSignal`, `model`). Ver [skills/angular-signals-estado](skills/angular-signals-estado/SKILL.md).
- **Signal Forms**: API de formularios basada en signals (`@angular/forms/signals`), estable en Angular ≥ 22. Ver [skills/angular-formularios](skills/angular-formularios/SKILL.md).
- **ADR**: Architecture Decision Record. Ver [decisions/TEMPLATE.md](decisions/TEMPLATE.md).
