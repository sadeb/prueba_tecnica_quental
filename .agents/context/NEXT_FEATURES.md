# Próximas funcionalidades y cierre de verificación

Estado: `ACTIVO`

Este documento convierte la revisión de requisitos realizada el 2026-09-11 en trabajo
ejecutable. Debe leerse antes de ampliar funcionalidades: primero se cierran los huecos
de evidencia del alcance ya implementado.

## Orden de prioridad

### P0 — Evidencia backend y contrato HTTP

1. Ampliar las pruebas `MockMvc` de login y alta administrativa ya incorporadas con
   token inválido/expirado/revocado y los casos restantes de `401` y `403`.
2. Añadir pruebas de favoritos: alta, baja idempotente y aislamiento entre usuarios.
   El listado paginado y ordenado ya está cubierto por
   `FavoriteServiceIntegrationTest`; faltan pruebas HTTP con `MockMvc` y las demás
   operaciones de la ruta.
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

1. Conservar las pruebas ejecutadas de `authGuard` y `adminGuard` para sesiones
   ausentes, `USER` y `ADMIN` al modificar el árbol de rutas.
2. Probar el interceptor: cabecera Bearer, limpieza de sesión y redirección tras `401`.
3. Probar servicios HTTP con `HttpTestingController` y una pantalla con estados loading,
   empty y error.
4. Mantener Playwright fuera del proyecto salvo autorización explícita del usuario.

Evidencia ya incorporada para sincronización:

- La presentación de los estados y disparadores, la traducción del error de descarga y
  la condición de estado terminal cuentan con prueba unitaria.
- La pantalla administrativa cuenta con prueba de componente que verifica el sondeo de
  una sincronización manual a los 60 segundos y su detención al completarse.

Pendiente: conservar esta cobertura al modificar el contrato `SyncRun` y añadir pruebas
para los demás servicios HTTP y flujos indicados arriba. El cliente de alta
administrativa ya cuenta con una prueba HTTP ejecutada correctamente. La ampliación del
cliente y la pantalla de gestión de usuarios incluye pruebas de listado, ordenación,
alta por diálogo y borrado confirmado, además de una prueba MockMvc de ordenación con
lista blanca; su ejecución posterior al rediseño del datatable sigue pendiente de
autorización, igual que la QA responsive de la tabla, los diálogos y los toasts.

Criterio de cierre: las rutas privadas, la expiración y el manejo de errores están
cubiertos por pruebas reproducibles.

### P1 — Entrega e infraestructura

1. Ejecutar desde una base limpia `cp .env.example .env` y `docker compose up --build`.
2. Verificar salud de los seis servicios, bootstrap del administrador, login, alta de
   un usuario desde la cuenta administrativa, una
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
| Backend `./mvnw -B verify` con Java 11 | Correcto; 10 pruebas, JAR generado |
| JaCoCo | 41,5 % líneas; 30,6 % ramas |
| Frontend `npm test -- --watch=false` | Correcto; 8 pruebas |
| Frontend `npm run build` con Node 24.16.0 | Correcto; versión compatible con el mínimo `^24.15.0` |
| `npm audit` | 0 vulnerabilidades reportadas |
| `docker compose config --quiet` | Correcto |
| `docker compose up -d --build backend frontend` | Correcto sobre volúmenes locales existentes; seis servicios saludables. Pendiente repetir desde una base limpia |

No se debe convertir ningún resultado pendiente en una afirmación de cumplimiento total.
