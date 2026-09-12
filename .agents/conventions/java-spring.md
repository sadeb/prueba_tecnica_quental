# Convenciones backend (Java 11 · Spring Boot 2.7)

## Arquitectura: capas por funcionalidad, dependencias hacia dentro
```
com.quental.rickmorty
├── config/            # beans: RestTemplate, Kafka topics, Security, OpenAPI, properties
├── common/            # ApiError, excepciones base, GlobalExceptionHandler, PageResponse
├── external/          # cliente de la fuente externa + DTOs External* + validación + mapper a dominio
├── sync/              # producer/, consumer/, SyncRunService, mensajes, DLT
├── character/ episode/ location/   # por entidad: entity, repository (jpa), graph (node, repo), service, controller, dto
├── graph/             # Neo4j común: constraints, cliente Cypher, RelatedCharacterQuery
├── user/              # User, registro, favoritos
└── auth/              # token propio, filtro, PasswordEncoder
```
- Controlador → Servicio → Repositorio. El controlador no toca repositorios ni el cliente externo. El cliente externo solo lo usa `sync/producer`.
- Sin `Lombok`, sin `MapStruct` (economía de dependencias): constructores explícitos, mapeadores como clases estáticas o `@Component` pequeños. Java 11: no hay `record`; usar clases finales inmutables con constructor.
- Inyección **por constructor**, campos `private final`. Nada de `@Autowired` en campos.
- DTOs de API separados de entidades JPA; nunca exponer entidades.
- Validación de entrada con `javax.validation` en DTOs `Request` + `@Valid` en controlador.
- Excepciones de dominio: `NotFoundException`, `ConflictException`, `ExternalServiceException`, `InvalidExternalPayloadException` → mapeadas en `GlobalExceptionHandler` ([formato-error.md](formato-error.md)).
- Logs con SLF4J, nivel `INFO` para hitos de sync (páginas, mensajes, DLT), `WARN` para fallos recuperables, `ERROR` para irrecuperables. Sin `System.out`.
- Configuración con `@ConfigurationProperties`; sin `@Value` disperso.
- Métodos cortos, nombres que expliquen la intención, sin comentarios que repitan el código. Comentario solo para el **porqué** no evidente (enlazar ADR).
- Tests como ciudadanos de primera: ver [references/testing-backend.md](../references/testing-backend.md).

Relacionado: [nomenclatura.md](nomenclatura.md), [references/spring-boot-2.7.md](../references/spring-boot-2.7.md).
