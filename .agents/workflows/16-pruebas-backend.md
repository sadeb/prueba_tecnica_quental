# 16 · Pruebas backend (incremental)

**Rol**: [qa-tester](../agents/qa-tester.md) · Skill: [testing-aislado](../skills/testing-aislado/SKILL.md)

## Objetivo
Cumplir [spec/07](../spec/07-pruebas.md) puntos 1–3 en backend. Se ejecuta por partes tras cada workflow 03–10; esta lista es el inventario completo.

## Contexto
[references/testing-backend.md](../references/testing-backend.md), ADRs [002](../decisions/ADR-002-idempotencia-sync.md), [004](../decisions/ADR-004-consistencia-postgres-neo4j.md), [005](../decisions/ADR-005-autenticacion.md), [006](../decisions/ADR-006-mensajes-irrecuperables.md).

## Inventario mínimo
| Test | Nivel | Cubre |
|---|---|---|
| `RickAndMortyClientTest` | MockRestServiceServer | OK, 404, 500+retry, timeout, JSON inválido |
| `ExternalPayloadValidatorTest`, `*MapperTest` | unitario | campos vacíos, origin sin url, ids no numéricos |
| `TokenServiceTest` | unitario | roundtrip, firma alterada, caducado, formato |
| `AuthControllerTest`, `FavoriteControllerTest`, `CharacterControllerTest` | `@WebMvcTest` | códigos, validación, `ApiError`, 401 |
| `*PersistenceServiceTest` | `@DataJpaTest` H2 | upsert idempotente, placeholders, reemplazo N:M |
| `RelatedCharactersServiceTest` | unitario | orden, límite, ids sin fila omitidos |
| **`SyncFlowIT`** (obligatorio) | `@SpringBootTest` + `@EmbeddedKafka` + H2, Neo4j mockeado | publicar → consumir → filas; doble publicación → mismos conteos; JSON corrupto → `.DLT` y el siguiente mensaje se procesa |
| `SyncProducerServiceTest` | unitario con cliente mockeado | paginación completa, página fallida → `PARTIAL`, elemento inválido → `skipped` |

## Pasos
1. Tabla de casos del módulo en curso.
2. Escribir tests; fábricas en `TestData`.
3. Perfil `test` autocontenido: sin Docker, sin red.

## Hecho cuando
`./mvnw test` verde sin servicios externos en la máquina (humano).

## Ejecuta y pega
```bash
cd projects/backend && ./mvnw test 2>&1 | grep -E "Tests run:|FAIL|ERROR\]" | tail -20
```

## Commit propuesto
`test(backend): cover external client, sync flow with embedded kafka, auth and api edge cases`
