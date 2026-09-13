# OpenAPI con springdoc 1.8.0 (Boot 2.7)

- Dependencia: `org.springdoc:springdoc-openapi-ui:1.8.0` (incluye Swagger UI; última 1.x). Rama 1.x = Spring Boot 2.x / javax. **No** usar springdoc 2.x (Boot 3) ni springfox (abandonado).
- Rutas: `/v3/api-docs` (JSON) y `/swagger-ui.html`. Excluirlas del filtro de autenticación.
- Activación por entorno: `springdoc.api-docs.enabled` y `springdoc.swagger-ui.enabled` leen `${SWAGGER_ENABLED:true}`; con `false` springdoc no registra sus controladores (404) y el bean `OpenAPI` no se crea ([ADR-009](../decisions/ADR-009-openapi-toggle.md)).
- Anotaciones mínimas y útiles: `@Tag` por controlador, `@Operation(summary)`, `@ApiResponse(responseCode, description, content)` para 200/201/204/400/401/404/409, `@Parameter` para filtros y paginación, `@Schema` en DTOs.
- Definir `SecurityScheme` tipo `http`/`bearer` en un bean `OpenAPI` y `@SecurityRequirement` en endpoints protegidos.
- El formato de error único ([conventions/formato-error.md](../conventions/formato-error.md)) se documenta una vez como `ApiError` y se referencia en todas las respuestas de error.
- Ocultar el endpoint `/api/admin/sync` no: documentarlo con tag `Admin`.
- Paginación: parámetros explícitos `page`/`size` con `@Min/@Max` (no se usa `Pageable`: springdoc 1.x lo documenta bien solo con el módulo `springdoc-openapi-data-rest`, una dependencia más).

Relacionado: [spec/05-api-propia.md](../spec/05-api-propia.md), [conventions/api-rest.md](../conventions/api-rest.md), [workflows/11-openapi.md](../workflows/11-openapi.md).
