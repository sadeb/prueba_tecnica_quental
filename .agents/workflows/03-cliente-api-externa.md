# 03 · Cliente de la API externa

**Rol**: [desarrollador-backend](../agents/desarrollador-backend.md) · Skill: [spring-boot-jdk11](../skills/spring-boot-jdk11/SKILL.md)

## Objetivo
Paquete `external/` que descarga páginas de Character, Episode y Location, tolera fallos, valida y transforma al modelo propio.

## Contexto
[spec/02](../spec/02-integracion-api-externa.md), [references/rick-and-morty-api.md](../references/rick-and-morty-api.md), [ADR-001](../decisions/ADR-001-identificador-externo.md), [references/testing-backend.md](../references/testing-backend.md).

## Pasos
1. `ExternalApiProperties` (`base-url`, `connect-timeout`, `read-timeout`, `max-retries`, `retry-backoff`).
2. `RestTemplate` dedicado vía `RestTemplateBuilder` con timeouts.
3. DTOs `ExternalPage<T>`, `ExternalCharacter`, `ExternalEpisode`, `ExternalLocation` (Jackson, `@JsonIgnoreProperties(ignoreUnknown = true)`), sin uso fuera de `external/`.
4. `RickAndMortyClient` con `fetchCharacters(page)`, `fetchEpisodes(page)`, `fetchLocations(page)`: reintento simple (bucle, backoff fijo) para timeouts/5xx; 404 en página → `ExternalPageNotFoundException`; otros → `ExternalServiceException`.
5. `ExternalPayloadValidator`: `results` no nulo, `id > 0`, `name` no vacío, URLs de relación con id entero. Elemento inválido → `InvalidExternalPayloadException` con detalle.
6. Mapeadores `External* → dominio` (`CharacterSnapshot`, `EpisodeSnapshot`, `LocationSnapshot`): `""` → `null`, `origin.url == ""` → `originExternalId = null`, extracción de ids de URLs.
7. Tests con `MockRestServiceServer`: OK, 404, 500 con reintento, timeout, JSON sin `results`, elemento con URL no numérica.

## Hecho cuando
Cliente sin dependencia de controladores ni servicios de dominio; tests del paso en verde (humano).

## Ejecuta y pega
```bash
cd backend && ./mvnw -q test -Dtest='RickAndMortyClientTest,ExternalPayloadValidatorTest,*MapperTest' && echo TESTS_OK
```
Pegar solo `Tests run:` y fallos.

## Commit propuesto
`feat(external): add fault-tolerant rick and morty client with validation and domain mapping`
