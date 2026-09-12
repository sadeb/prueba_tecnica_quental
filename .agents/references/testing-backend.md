# Pruebas backend (JUnit 5, Boot 2.7)

Requisito: [spec/07-pruebas.md](../spec/07-pruebas.md). Skill: [testing-aislado](../skills/testing-aislado/SKILL.md).

## Niveles
| Nivel | Herramienta | Uso |
|---|---|---|
| Unitario | JUnit 5 + Mockito (en `spring-boot-starter-test`) | Mapeadores, validadores, servicios, generador/validador de token |
| Cliente externo | `MockRestServiceServer` (RestTemplate) | Respuestas OK, 404, 500, timeout, JSON malformado, campos ausentes. **Sin red real** |
| Web | `@WebMvcTest` + `MockMvc` | Códigos de estado, formato de error, filtros, validación de entrada, 401/403 |
| JPA | `@DataJpaTest` con H2 en modo PostgreSQL (`jdbc:h2:mem:db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`) | Upsert por `external_id`, unicidad, favoritos |
| Sync (obligatoria, más allá de unidad) | `@SpringBootTest` + `@EmbeddedKafka` + H2 + Neo4j mockeado o embebido | Publicar → consumir → persistir; reprocesar el mismo mensaje no duplica; mensaje corrupto va al DLT |
| Neo4j (opcional) | `neo4j-harness` 4.4 o Testcontainers `Neo4jContainer` | Consulta de relacionados devuelve orden correcto |

## Reglas
- `@EmbeddedKafka` no necesita Docker: cumple "sin infraestructura instalada manualmente". Testcontainers requiere Docker en la máquina del evaluador; si se usa, marcarlo con `@Testcontainers` y `@DisabledIfEnvironmentVariable` de forma que `mvn test` no falle sin Docker, o dejarlo como perfil opcional.
- Perfil `test` con `application-test.yml`: H2, Kafka embebido (`spring.embedded.kafka.brokers`), Neo4j deshabilitado o apuntando al embebido.
- Casos límite obligatorios: página vacía, `origin.url == ""`, id no numérico en URL de episodio, respuesta externa con campo faltante, mensaje duplicado, mensaje JSON inválido, favorito repetido (409 o idempotente), token caducado, token manipulado.
- Nombres: `<Clase>Test` (unitario), `<Clase>IT` o `<Flujo>IntegrationTest` (integración). Patrón `given/when/then` en el nombre o en comentarios breves.
- El agente **no ejecuta** `mvn test` ([no-run-commands](../skills/no-run-commands/SKILL.md)); pide al humano la salida.

Relacionado: [workflows/16-pruebas-backend.md](../workflows/16-pruebas-backend.md), [kafka-2.0.1.md](kafka-2.0.1.md).
