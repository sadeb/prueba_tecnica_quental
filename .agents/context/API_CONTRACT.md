# Contrato HTTP implementado

Estado: `IMPLEMENTADO; OPENAPI ES LA REFERENCIA EJECUTABLE`

Prefijo implementado: `/api/v1`. Springdoc genera la especificación ejecutable en
`/v3/api-docs` y Swagger UI en `/swagger-ui.html`.

## Convenciones

- JSON para solicitudes y respuestas.
- Fechas en ISO 8601 y horas en UTC.
- Paginación con `page` base cero y `size` acotado; respuesta con contenido y metadatos de página.
- Identificadores expuestos como valores estables propios, manteniendo `externalId` cuando aporte trazabilidad.
- Token opaco en `Authorization: Bearer <token>`.
- Errores con una única forma: `timestamp`, `status`, `code`, `message`, `path`, `fieldErrors` opcional y `traceId` cuando exista.
- La documentación OpenAPI genera rutas, parámetros y esquemas desde los controladores
  y declara `opaqueBearer` en las operaciones protegidas. Pendiente: documentar todos
  los códigos de error por operación, incluyendo ejemplos representativos.

## Autenticación

| Método y ruta | Acceso | Resultado esperado |
| --- | --- | --- |
| `POST /api/v1/auth/login` | Público | Emite token opaco y metadatos de expiración |
| `POST /api/v1/auth/logout` | Autenticado | Revoca el token actual de forma idempotente |
| `GET /api/v1/auth/me` | Autenticado | Devuelve identidad y roles de la sesión |

No se expondrá autorregistro, recuperación de contraseña, OAuth ni refresh token salvo
decisión posterior explícita.

## Catálogo

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `GET /api/v1/characters` | Autenticado | Lista paginada con filtros por nombre, estado, especie, tipo y género |
| `GET /api/v1/characters/{characterId}` | Autenticado | Detalle con episodios, origen y ubicación actual |
| `GET /api/v1/characters/{characterId}/related` | Autenticado | Relacionados calculados en Neo4j, ordenados por episodios comunes |

Un recurso inexistente devuelve `404`; filtros inválidos o paginación fuera de límites devuelven `400`. El tamaño máximo es 100 elementos por página.

## Favoritos

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `GET /api/v1/users/me/favorites` | Autenticado | Lista paginada de personajes favoritos |
| `PUT /api/v1/users/me/favorites/{characterId}` | Autenticado | Añade de forma idempotente |
| `DELETE /api/v1/users/me/favorites/{characterId}` | Autenticado | Elimina de forma idempotente |

La identidad de usuario se obtiene del token y nunca de un identificador enviado por el cliente.

## Administración

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `GET /api/v1/admin/users` | `ADMIN` | Lista paginada y filtrable mediante `search`; admite `sort` (`username`, `role`, `enabled`, `createdAt`, `id`) y `direction` (`asc`/`desc`), con `username` ascendente por defecto y `id` como desempate |
| `POST /api/v1/admin/users` | `ADMIN` | Crea una cuenta habilitada con rol `USER`; nunca inicia sesión por ella |
| `PUT /api/v1/admin/users/{userId}` | `ADMIN` | Actualiza nombre, estado y opcionalmente contraseña; el rol no es editable |
| `DELETE /api/v1/admin/users/{userId}` | `ADMIN` | Elimina una cuenta `USER`, sus sesiones y favoritos mediante cascada |
| `POST /api/v1/admin/sync-runs` | `ADMIN` | Inicia una ejecución y devuelve `202 Accepted` con su identificador |
| `GET /api/v1/admin/sync-runs/{syncRunId}` | `ADMIN` | Expone estado, contadores y fallos parciales |
| `GET /api/v1/admin/sync-runs` | `ADMIN` | Historial paginado de ejecuciones |

La petición de inicio no espera a que finalice toda la sincronización. El contrato de
mensajería, reintentos y DLT está concretado en ADR-0006. Varias ejecuciones pueden
iniciarse; los datos convergen por las garantías de idempotencia, pero no hay una llave
de idempotencia HTTP para deduplicar solicitudes de inicio.

Cada ejecución incluye `trigger`, con valor `MANUAL` cuando procede del endpoint y
`AUTOMATIC` cuando la inicia la configuración de arranque. Ambos tipos se persisten y
aparecen en el mismo historial.

Un valor de `sort` o `direction` fuera de los admitidos responde `400` con código
`INVALID_REQUEST`.

Las cuentas `ADMIN` aparecen en el listado, pero no se pueden editar ni eliminar.
El cambio de contraseña de una cuenta `USER` es opcional durante la edición y nunca expone el hash existente.

## Códigos transversales

- `200`/`201` para lecturas y creación completada.
- `202` para trabajo asíncrono aceptado.
- `204` para revocación/eliminación idempotente sin cuerpo.
- `400` para validación, filtros o formato inválido.
- `401` para credenciales/token ausente, inválido, expirado o revocado.
- `403` para rol insuficiente.
- `404` para recurso inexistente.
- `409` para conflictos de unicidad de usuario u otro conflicto de dominio documentado.
- `429` si se incorpora un límite explícito de solicitudes.
- `502`/`503` para dependencias no disponibles cuando la operación no pueda degradarse de forma segura.
