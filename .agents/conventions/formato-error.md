# Formato de error homogéneo

Toda respuesta de error de la API propia (4xx/5xx) usa el mismo cuerpo, producido únicamente por `GlobalExceptionHandler` (`@RestControllerAdvice`):

```json
{
  "timestamp": "2026-09-12T10:15:30Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Character 999 not found",
  "path": "/api/characters/999",
  "details": [ { "field": "size", "message": "must be less than or equal to 100" } ]
}
```

| Situación | status | error |
|---|---|---|
| Validación de cuerpo/params (`MethodArgumentNotValidException`, `ConstraintViolationException`, `MethodArgumentTypeMismatchException`) | 400 | `VALIDATION_ERROR` (+ `details`) |
| Credenciales incorrectas / token ausente, inválido o caducado | 401 | `UNAUTHORIZED` |
| Token válido sin permiso (si aplica) | 403 | `FORBIDDEN` |
| Recurso inexistente (`NotFoundException`, ruta no mapeada) | 404 | `NOT_FOUND` |
| Conflicto (usuario existente, sync en curso) | 409 | `CONFLICT` |
| Fallo de la fuente externa durante una operación síncrona | 502 | `EXTERNAL_SERVICE_ERROR` |
| Neo4j/Postgres/Kafka no disponibles | 503 | `SERVICE_UNAVAILABLE` |
| Cualquier otra excepción | 500 | `INTERNAL_ERROR` (mensaje genérico; detalle solo en log) |

- `details` solo cuando hay errores de campo; en otro caso se omite o va vacío (elegir uno y mantenerlo).
- Nunca filtrar stack traces ni mensajes internos de Hibernate/Neo4j al cliente.
- El frontend modela este cuerpo como `ApiError` y lo muestra en `error-alert` ([conventions/angular.md](angular.md)).
- Documentado una vez en OpenAPI como esquema `ApiError` ([references/openapi-springdoc.md](../references/openapi-springdoc.md)).

Relacionado: [api-rest.md](api-rest.md), [spec/05-api-propia.md](../spec/05-api-propia.md).
