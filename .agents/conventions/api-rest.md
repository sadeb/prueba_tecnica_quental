# Contrato de la API propia

Prefijo `/api`. JSON UTF-8. Errores según [formato-error.md](formato-error.md). Autenticación `Authorization: Bearer <token>` ([ADR-005](../decisions/ADR-005-autenticacion.md)).

| Método y ruta | Auth | Respuesta OK | Errores |
|---|---|---|---|
| `POST /api/auth/register` `{username, password}` | no | 201 `{id, username}` | 400 validación, 409 usuario existe |
| `POST /api/auth/login` `{username, password}` | no | 200 `{token, expiresAt, username}` | 400, 401 credenciales |
| `GET /api/characters?name&status&species&gender&page&size` | no | 200 `PageResponse<CharacterSummary>` | 400 filtro inválido |
| `GET /api/characters/{id}` | no | 200 `CharacterDetail` (origin, location, episodes) | 404 |
| `GET /api/characters/{id}/related?limit=10` | no | 200 `[ {character, sharedEpisodes} ]` (Neo4j) | 404 |
| `GET /api/episodes?page&size` · `GET /api/episodes/{id}` | no | 200 | 404 |
| `GET /api/locations?page&size` · `GET /api/locations/{id}` (+ `residents`) | no | 200 | 404 |
| `GET /api/users/me/favorites` | sí | 200 `[CharacterSummary]` | 401 |
| `POST /api/users/me/favorites/{characterId}` | sí | 201 `CharacterSummary` al crear; **200** con el mismo cuerpo si ya era favorito (idempotente) | 401, 404 personaje |
| `DELETE /api/users/me/favorites/{characterId}` | sí | 204 | 401, 404 favorito |
| `POST /api/admin/sync` | ver ADR-005 | 202 `{runId, startedAt}` | 409 sync en curso |
| `GET /api/admin/sync/{runId}` | ver ADR-005 | 200 estado y contadores | 404 |
| `GET /v3/api-docs` · `GET /swagger-ui.html` | no | OpenAPI JSON y Swagger UI; 404 si `SWAGGER_ENABLED=false` ([ADR-009](../decisions/ADR-009-openapi-toggle.md)) | — |

## Reglas
- `id` en rutas = **id interno** de Postgres ([ADR-001](../decisions/ADR-001-identificador-externo.md)); las respuestas incluyen también `externalId`.
- Paginación: `page` base 0, `size` por defecto 20, máximo 100. `PageResponse { content, page, size, totalElements, totalPages }`.
- Filtros de texto: `name` búsqueda parcial case-insensitive; `status`/`gender` valores del enum (400 si no válido).
- Listados sin auth: los datos sincronizados son públicos; solo `/api/users/me/**` y `/api/admin/**` requieren token; el resto es `permitAll` para que una ruta no mapeada responda 404 y no 401 ([formato-error](formato-error.md)).
- Método no soportado → 405 `METHOD_NOT_ALLOWED` (añadido a [formato-error](formato-error.md)).
- Detalle de una entidad todavía `placeholder` (referenciada pero no sincronizada) → 404; el listado no la incluye.
- Versionado de API: no se pide; no añadir `/v1`.
- Todo endpoint documentado en OpenAPI ([references/openapi-springdoc.md](../references/openapi-springdoc.md)).

Relacionado: [spec/05-api-propia.md](../spec/05-api-propia.md), [workflows/10-api-consulta.md](../workflows/10-api-consulta.md).
