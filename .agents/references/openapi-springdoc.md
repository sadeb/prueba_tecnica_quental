# OpenAPI con springdoc 1.7.0 (Boot 2.7)

- Dependencia: `org.springdoc:springdoc-openapi-ui:1.7.0` (incluye Swagger UI). Rama 1.x = Spring Boot 2.x / javax. **No** usar springdoc 2.x (Boot 3) ni springfox (abandonado).
- Rutas: `/v3/api-docs` (JSON) y `/swagger-ui.html`. Excluirlas del filtro de autenticación.
- Anotaciones mínimas y útiles: `@Tag` por controlador, `@Operation(summary)`, `@ApiResponse(responseCode, description, content)` para 200/201/204/400/401/404/409, `@Parameter` para filtros y paginación, `@Schema` en DTOs.
- Definir `SecurityScheme` tipo `http`/`bearer` en un bean `OpenAPI` y `@SecurityRequirement` en endpoints protegidos.
- El formato de error único ([conventions/formato-error.md](../conventions/formato-error.md)) se documenta una vez como `ApiError` y se referencia en todas las respuestas de error.
- Ocultar el endpoint `/api/admin/sync` no: documentarlo con tag `Admin`.
- Verificar que los parámetros `Pageable` se muestran como `page`, `size`, `sort` (springdoc lo hace con `@ParameterObject`).

Relacionado: [spec/05-api-propia.md](../spec/05-api-propia.md), [conventions/api-rest.md](../conventions/api-rest.md), [workflows/11-openapi.md](../workflows/11-openapi.md).
