# Nomenclatura

| Ámbito | Convención | Ejemplo |
|---|---|---|
| Paquete raíz Java | `com.quental.rickmorty` | — |
| Clases Java | PascalCase, sufijo por rol | `CharacterController`, `CharacterService`, `CharacterJpaRepository`, `CharacterGraphRepository`, `RickAndMortyClient`, `CharacterMapper` |
| DTO API propia | sufijo `Request`/`Response` | `LoginRequest`, `CharacterDetailResponse`, `PageResponse<T>` |
| DTO del proveedor externo | prefijo `External` | `ExternalCharacter`, `ExternalPage<T>` |
| Mensaje Kafka | sufijo `Message` | `CharacterSyncMessage` |
| Entidad JPA | nombre de dominio, sin sufijo | `Character`, `Episode`, `Location`, `User`, `Favorite` |
| Nodo Neo4j | sufijo `Node` | `CharacterNode` |
| Tablas / columnas | `snake_case`, tablas en plural | `characters.external_id`, `user_favorites` |
| Topics Kafka | `rm.<entidad-plural>` y `rm.<entidad-plural>.DLT` | `rm.characters`, `rm.characters.DLT` |
| Consumer group | `rm-sync-consumer` | — |
| Rutas API | `/api/<recurso-plural>`, kebab-case | `/api/characters/{id}/related` |
| Angular ficheros | kebab-case + tipo | `character-list.component.ts`, `auth.guard.ts`, `auth.interceptor.ts`, `character.service.ts` |
| Angular clases | PascalCase + sufijo | `CharacterListComponent`, `AuthService` |
| Modelos TS | interfaces sin prefijo `I` | `Character`, `PageResponse<T>`, `ApiError` |
| Propiedades config | kebab-case agrupado | `external-api.base-url`, `sync.topics.characters` |

Idioma del código, comentarios y commits: **inglés**. Idioma del README y ADRs: **español** (la entrevista es en español).

Relacionado: [java-spring.md](java-spring.md), [angular.md](angular.md).
