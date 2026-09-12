# 11 · Documentación OpenAPI

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) / [redactor-docs](../agents/redactor-docs.md)

## Objetivo
`/v3/api-docs` y Swagger UI que reflejen fielmente todos los endpoints, parámetros, filtros, respuestas y códigos.

## Contexto
[references/openapi-springdoc.md](../references/openapi-springdoc.md), [conventions/api-rest.md](../conventions/api-rest.md), [conventions/formato-error.md](../conventions/formato-error.md).

## Pasos
1. Bean `OpenAPI` con `info`, `SecurityScheme` bearer y esquema `ApiError` reutilizable.
2. Por controlador: `@Tag`; por endpoint: `@Operation`, `@ApiResponses` con todos los códigos de [api-rest](../conventions/api-rest.md); `@Parameter` en filtros; `@ParameterObject` en `Pageable`; `@SecurityRequirement` en protegidos.
3. `@Schema(description, example)` en DTOs de request/response.
4. Excluir `/v3/api-docs/**` y `/swagger-ui/**` de la seguridad.
5. Revisión cruzada: cada fila de [api-rest.md](../conventions/api-rest.md) aparece en el JSON con los mismos códigos.

## Hecho cuando
El humano abre `http://localhost:8080/swagger-ui.html`, ve todos los endpoints y puede autenticarse con "Authorize".

## Ejecuta y pega
```bash
curl -s http://localhost:8080/v3/api-docs | python3 -c "import json,sys; d=json.load(sys.stdin); [print(m.upper(), p, sorted(o['responses'])) for p,ops in d['paths'].items() for m,o in ops.items()]"
```

## Commit propuesto
`docs(api): document endpoints, parameters and error responses with springdoc`
