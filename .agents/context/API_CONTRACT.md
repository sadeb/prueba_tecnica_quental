# Contrato HTTP objetivo

Estado: `IMPLEMENTADO; OPENAPI ES LA REFERENCIA EJECUTABLE`

Prefijo previsto: `/api/v1`. Los nombres son parte de la dirección del diseño, pero los esquemas OpenAPI definitivos se escribirán en la fase de API.

## Convenciones

- JSON para solicitudes y respuestas.
- Fechas en ISO 8601 y horas en UTC.
- Paginación con `page` base cero y `size` acotado; respuesta con contenido y metadatos de página.
- Identificadores expuestos como valores estables propios, manteniendo `externalId` cuando aporte trazabilidad.
- Token opaco en `Authorization: Bearer <token>`.
- Errores con una única forma: `timestamp`, `status`, `code`, `message`, `path`, `fieldErrors` opcional y `traceId` cuando exista.
- La documentación OpenAPI describirá autenticación, parámetros, ejemplos y todos los códigos relevantes.

## Autenticación

| Método y ruta | Acceso | Resultado esperado |
| --- | --- | --- |
| `POST /api/v1/auth/register` | Público | Crea un usuario no administrativo con credenciales válidas |
| `POST /api/v1/auth/login` | Público | Emite token opaco y metadatos de expiración |
| `POST /api/v1/auth/logout` | Autenticado | Revoca el token actual de forma idempotente |
| `GET /api/v1/auth/me` | Autenticado | Devuelve identidad y roles de la sesión |

No se expondrá recuperación de contraseña, OAuth ni refresh token salvo decisión posterior explícita.

## Catálogo

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `GET /api/v1/characters` | Público | Lista paginada con filtros por nombre, estado, especie, tipo y género |
| `GET /api/v1/characters/{characterId}` | Público | Detalle con episodios, origen y ubicación actual |
| `GET /api/v1/characters/{characterId}/related` | Público | Relacionados calculados en Neo4j, ordenados por episodios comunes |

Un recurso inexistente devuelve `404`; filtros inválidos o paginación fuera de límites devuelven `400`. El tamaño máximo es 100 elementos por página.

## Favoritos

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `GET /api/v1/users/me/favorites` | Autenticado | Lista paginada de personajes favoritos |
| `PUT /api/v1/users/me/favorites/{characterId}` | Autenticado | Añade de forma idempotente |
| `DELETE /api/v1/users/me/favorites/{characterId}` | Autenticado | Elimina de forma idempotente |

La identidad de usuario se obtiene del token y nunca de un identificador enviado por el cliente.

## Administración de sincronización

| Método y ruta | Acceso | Comportamiento |
| --- | --- | --- |
| `POST /api/v1/admin/sync-runs` | `ADMIN` | Inicia una ejecución y devuelve `202 Accepted` con su identificador |
| `GET /api/v1/admin/sync-runs/{syncRunId}` | `ADMIN` | Expone estado, contadores y fallos parciales |
| `GET /api/v1/admin/sync-runs` | `ADMIN` | Historial paginado de ejecuciones |

La petición de inicio no esperará a que finalice toda la sincronización. Reintentos accidentales se controlarán mediante una política idempotente que deberá concretarse en el ADR de mensajería.

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
