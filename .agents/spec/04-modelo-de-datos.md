# Requisito 3 · Modelo de datos y relaciones

Persistencia **políglota**: PostgreSQL es la fuente de verdad de atributos y de toda la funcionalidad de usuario; Neo4j mantiene el grafo de relaciones entre entidades sincronizadas.

## Entidades sincronizadas (3)
| Entidad | Campos mínimos | Relaciones |
|---|---|---|
| Character | name, status, species, type, gender, image | N:M con Episode · origen y ubicación actual → Location |
| Episode | name, air_date, código de episodio (`S01E01`) | N:M con Character |
| Location | name, type, dimension | Residentes (inverso de la ubicación actual de Character) |

## Entidad de usuario (solo PostgreSQL)
| Entidad | Campos |
|---|---|
| Usuario | credenciales y favoritos (referencia a Character) |

Se admite ampliar campos si se justifica.

## Reglas
- Character ↔ Episode: muchos a muchos.
- Character → Location: cada personaje referencia **dos** localizaciones distintas (origen y actual).
- La correspondencia registro relacional ↔ nodo del grafo debe ser **estable y reproducible** tras varias sincronizaciones.
- Al menos una consulta de la API propia se resuelve en **Neo4j** aprovechando el grafo. Ejemplo: personajes relacionados con uno dado por aparecer en episodios comunes, ordenados por número de coincidencias.
- La estrategia para mantener **coherentes ambos almacenes ante fallos parciales** se evalúa.
- El tratamiento del **identificador externo** y de **campos ausentes o inconsistentes** se evalúa; no hay solución prescrita.

Relacionado:
- [decisions/ADR-001-identificador-externo.md](../decisions/ADR-001-identificador-externo.md)
- [decisions/ADR-004-consistencia-postgres-neo4j.md](../decisions/ADR-004-consistencia-postgres-neo4j.md)
- [references/postgresql-10.md](../references/postgresql-10.md), [references/neo4j.md](../references/neo4j.md)
- Workflows: [06-persistencia-postgres.md](../workflows/06-persistencia-postgres.md), [07-persistencia-neo4j.md](../workflows/07-persistencia-neo4j.md)
