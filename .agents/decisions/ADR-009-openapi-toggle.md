# ADR-009 · OpenAPI y Swagger UI activables por variable de entorno

**Estado**: Propuesta · **Requisito**: [spec/05](../spec/05-api-propia.md) punto 1 (documentación OpenAPI); petición explícita del candidato de no exponer Swagger obligatoriamente en producción · **Fecha**: 2026-09-13

## Contexto
springdoc genera `/v3/api-docs` y sirve Swagger UI a partir de las anotaciones del código. Es imprescindible en desarrollo y evaluación, pero en un despliegue productivo expone el contrato completo y añade superficie HTTP. No se pide seguridad avanzada; basta con poder apagarlo sin recompilar.

## Decisión
- Una sola variable, `SWAGGER_ENABLED` (default `true`), alimenta las dos propiedades de springdoc: `springdoc.api-docs.enabled=${SWAGGER_ENABLED:true}` y `springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:true}`. Con `false` springdoc no registra sus controladores ni recursos: `/v3/api-docs` y `/swagger-ui.html` responden 404.
- El bean `OpenAPI` propio (`config/OpenApiConfig`, info + esquema `bearerAuth`) lleva `@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)`: cuando se apaga no queda ni la configuración.
- Las rutas de springdoc son públicas en `SecurityConfig` (todo lo no protegido es `permitAll`); apagado el módulo no hay nada que proteger.
- `projects/.env` declara `SWAGGER_ENABLED=true` y el servicio `backend` del compose lo pasa como `${SWAGGER_ENABLED:-true}`. El perfil `test` lo fija a `false` (los slices web no lo necesitan).

## Alternativas descartadas
- Perfil de Spring `prod` que excluya springdoc — acopla el apagado a la gestión de perfiles y obliga a recordar dos mecanismos (perfil + variables).
- Proteger Swagger con autenticación en vez de apagarlo — sigue exponiendo el contrato a cualquier usuario registrado y añade lógica de seguridad no pedida.
- Dependencia con `<optional>` o classifier de build — exige recompilar para cambiar el comportamiento.

## Consecuencias
Un interruptor de una línea, verificable con `curl -I /v3/api-docs` (200 / 404). En entrevista: "la documentación se genera del código y se apaga por configuración, no por compilación". Vigilar: si alguna vez se protege todo con `anyRequest().authenticated()`, habrá que volver a listar las rutas de springdoc como públicas.

## Dónde se aplica
`projects/backend/src/main/resources/application.yml`, `config/OpenApiConfig.java`, `config/SecurityConfig.java`, `projects/.env`, `projects/docker-compose.yml`; [references/openapi-springdoc.md](../references/openapi-springdoc.md), [workflows/11](../workflows/11-openapi.md).
