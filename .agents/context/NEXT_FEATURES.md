# Próximas funcionalidades y cierre de verificación

Estado: `ACTIVO`

Este documento convierte la revisión de requisitos realizada el 2026-09-11 en trabajo
ejecutable. Debe leerse antes de ampliar funcionalidades: primero se cierran los huecos
de evidencia del alcance ya implementado.

## Orden de prioridad

### P0 — Evidencia backend y contrato HTTP

1. Añadir pruebas `MockMvc` de registro, login, token inválido/expirado/revocado,
   `401`, `403` y acceso `ADMIN`.
2. Añadir pruebas de favoritos: alta, listado, baja idempotente y aislamiento entre
   usuarios.
3. Completar anotaciones Springdoc: `SecurityRequirement` para operaciones protegidas y
   `ApiResponses` para `400`, `401`, `403`, `404`, `409`, `502` y `500` cuando proceda.
4. Probar que el bootstrap ADMIN crea un usuario desde entorno sin alterar uno existente.

Criterio de cierre: pruebas verdes y `/v3/api-docs` declara seguridad, parámetros,
esquemas y respuestas de error relevantes.

### P0 — Evidencia de mensajería y grafo

1. Añadir una prueba de mensaje inválido que compruebe reintentos, llegada a DLT y el
   incremento de fallo en su `sync_run`.
2. Añadir una prueba contra Neo4j real, preferiblemente Testcontainers, para verificar
   `MERGE`, relaciones y orden de personajes relacionados por episodios comunes.
3. Añadir una prueba de recuperación del outbox tras un fallo temporal del publicador.
4. Decidir y documentar una operación administrativa de reproceso de `raw_payloads` sin
   descargar de nuevo. Debe ser `ADMIN`, trazable e idempotente.

Criterio de cierre: se demuestra DLT, recuperación, proyección Neo4j y reproceso sin
consultar la API externa.

### P1 — Evidencia frontend

1. Probar `authGuard` y `adminGuard` para sesiones ausentes, `USER` y `ADMIN`.
2. Probar el interceptor: cabecera Bearer, limpieza de sesión y redirección tras `401`.
3. Probar servicios HTTP con `HttpTestingController` y una pantalla con estados loading,
   empty y error.
4. Mantener Playwright fuera del proyecto salvo autorización explícita del usuario.

Criterio de cierre: las rutas privadas, la expiración y el manejo de errores están
cubiertos por pruebas reproducibles.

### P1 — Entrega e infraestructura

1. Ejecutar desde una base limpia `cp .env.example .env` y `docker compose up --build`.
2. Verificar salud de los seis servicios, registro del administrador, login, una
   sincronización y consulta de relacionados.
3. Ejecutar una segunda sincronización y comprobar que no se duplican filas, relaciones
   ni favoritos.
4. Guardar únicamente resultados no sensibles en `HANDOFF.md`; eliminar el `.env` local
   si contiene credenciales antes de preparar commits.
5. Revisar las dependencias Testcontainers: usarlas en pruebas de integración o retirarlas
   para respetar economía de dependencias.

Criterio de cierre: Compose reproducible, sin secretos versionados, y escenarios de
reinicio/resincronización documentados.

## Convención para cambios y commits

- Código, tipos, rutas y scopes: inglés.
- Mensajes: Conventional Commits; tipo y alcance en inglés, descripción y cuerpo en
  español.
- Separar los cambios en commits lógicos. Propuesta actual:

```text
feat(backend): implementar API de sincronización de Rick and Morty
feat(frontend): implementar SPA Angular 22 de Rick and Morty
docs(proyecto): documentar arquitectura, contexto de agentes y ejecución local
```

- No crear commits ni modificar el historial Git salvo solicitud explícita del usuario.

## Estado de verificación conocido

| Comprobación | Resultado |
| --- | --- |
| Backend `./mvnw -B verify` con Java 11 | Correcto; 5 pruebas, JAR generado |
| JaCoCo | 41,5 % líneas; 30,6 % ramas |
| Frontend `npm test -- --watch=false` | Correcto; 4 pruebas |
| Frontend `npm run build` con Node 24.19.0 | Correcto |
| `npm audit` | 0 vulnerabilidades reportadas |
| `docker compose config --quiet` | Correcto |
| `docker compose up --build` | No ejecutado |

No se debe convertir ningún resultado pendiente en una afirmación de cumplimiento total.
