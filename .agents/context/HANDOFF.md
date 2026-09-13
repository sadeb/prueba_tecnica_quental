# Handoff actual

- Actualizado: 2026-09-13
- Fase: `8 - VERIFICATION`
- Estado: `IMPLEMENTACIÓN COMPLETA; CIERRE INTEGRAL PENDIENTE`

## Objetivo vigente

Cerrar la verificación integral de la solución implementada. El orden, los criterios y
las evidencias pendientes están en `NEXT_FEATURES.md`.

## Hecho en esta fase

- PDF de requisitos leído y revisado visualmente en sus tres páginas.
- Requisitos clasificados por procedencia.
- Contexto neutral para agentes y adaptadores mínimos preparados.
- Arquitectura objetivo y cuatro decisiones iniciales documentadas.
- Roadmap protegido mediante puertas de aprobación.
- Directorios de aplicación reservados sin scaffolds.
- El usuario instaló las skills Angular, frontend y Spring Boot.
- El usuario generó un scaffold Angular que la inspección identifica como 20.3.31.
- La API REST real y su documentación fueron inspeccionadas para fijar los DTO externos.
- Backend Java 11/Spring Boot 2.7.18 creado con Maven Wrapper 3.9.11, MVC, Security,
  JPA/Hibernate, Liquibase, Kafka, Neo4j, outbox y OpenAPI.
- SPA actualizada a Angular 22 con Bootstrap, rutas lazy, guards, interceptor, Signal
  Forms y pantallas de autenticación, catálogo, detalle, favoritos y sincronización.
- Compose, Dockerfiles, Nginx y configuración de entorno creados.
- Puerto HTTP del backend unificado en `8989` para ejecución local, Compose, healthcheck
  y proxies del frontend.
- Puertos publicados por Compose documentados y alineados con la configuración local:
  frontend `4400`, backend `4889`, PostgreSQL `4532`, Neo4j HTTP/Bolt `4774`/`4787` y
  Kafka `4992`. Kafka conserva listeners internos en `29092` y `9092`, y anuncia el
  puerto publicado `4992` exclusivamente a clientes del host.

## Decisiones vigentes

- `AGENTS.md` es la fuente canónica.
- Java 11, Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- Spring MVC, JPA/Hibernate y Liquibase como único gestor del esquema.
- PostgreSQL como fuente de verdad, Neo4j como proyección y Kafka como canal.
- Transactional outbox para consistencia y tokens opacos propios de 256 bits.
- Angular 22.1.x, Node 24.15 o superior compatible, TypeScript 6.0.x, Bootstrap; sin Playwright.
- Los bonus de autenticación, JSON crudo, DLT, mensajes procesados y Liquibase están
  implementados; la cobertura adicional y el reproceso explícito continúan pendientes.
- La ejecución de frontend, backend, Docker/Docker Compose y el uso del navegador
  integrado exige autorización explícita previa en la conversación actual; la skill
  local `project-execution-authorization` y `AGENTS.md` fijan esta restricción.

## Verificaciones

- `JAVA_HOME=.../microsoft-11.jdk/Contents/Home ./mvnw -B verify`: build y JAR correctos; 5 pruebas correctas,
  incluida Kafka embebida, migraciones Liquibase, validación Hibernate e idempotencia.
- Informe JaCoCo: 41,5 % de líneas y 30,6 % de ramas; útil como línea base, pero
  insuficiente para considerar cerrado el bonus de cobertura adicional.
- `npm test -- --watch=false`: 4 pruebas Angular correctas en ChromeHeadless.
- `npm run build`: compilación de producción Angular 22 correcta.
- `npm audit` y `npm audit --omit=dev`: 0 vulnerabilidades reportadas.
- `docker compose config --quiet`: configuración válida.
- `docker compose up -d --build backend` y posterior arranque del frontend: los seis
  servicios quedaron saludables; backend con readiness `UP` y frontend con respuesta
  HTTP `200`. Se corrigieron el intervalo del publicador outbox para Spring 5.3 usando
  un valor numérico con `TimeUnit.SECONDS` y el destino IPv4 del healthcheck de Nginx.
- `git diff --check`: correcto, sin errores de whitespace.
- Parseo de `.gemini/settings.json`: correcto.
- Resolución de enlaces Markdown locales: correcta.
- Adaptador `CLAUDE.md`: contiene únicamente `@AGENTS.md`.
- SPA ajustada con estilos mobile-first, shell basado en `100dvh`, contenedores fluidos,
  breakpoints Bootstrap y objetivos táctiles mínimos de 44 px.
- Las ejecuciones de sincronización registran su disparador `MANUAL` o `AUTOMATIC`; la
  sincronización al arranque queda trazada como automática en el historial.
- La pantalla de sincronización usa signals/computed y, tras iniciar una ejecución
  manual, consulta su estado cada 60 segundos hasta que termina.
- Los estados se presentan en español. El fallo `Unable to fetch character page 24` se
  muestra como un error de descarga contextualizado y el 100 % se identifica como
  progreso de mensajes.
- QA responsive de login, registro, catálogo, detalle y página no encontrada en 375x667,
  768x1024, 1024x600 y 1440x900: sin desbordamiento horizontal. Favoritos y
  sincronización redirigieron correctamente a login al no existir sesión.
- Login y registro encajan sin scroll en laptops de 1024x600 y 1366x768; en móvil el
  formulario se prioriza antes del bloque introductorio y el desplazamiento vertical es
  natural.
- Tras los ajustes responsive, `npm test -- --watch=false`: 4 pruebas correctas; y
  `npm run build`: compilación de producción Angular 22 correcta, ambos con Node 24.16.0.
- Tras la trazabilidad y el polling, `./mvnw -B verify`: 9 pruebas correctas;
  `npm test -- --watch=false`: 8 pruebas correctas; y `npm run build`: correcto.
- QA de sincronización en `375x667`, `768x1024`, `1024x600` y `1440x900`: sin
  desbordamiento horizontal; `FAILED` traducido como `Fallida`, origen visible y fallo
  de página contextualizado.
- Contexto persistente de UI creado en `FRONTEND_UI.md` y declarado como lectura
  obligatoria en `AGENTS.md` para cambios de interfaz, layout o estilos.
- Las tarjetas del catálogo y favoritos comparten la altura de la tarjeta más alta del
  grid; el artículo y su imagen rellenan la celda, y la acción queda alineada al pie sin
  depender de que el personaje tenga o no un tipo informado.
- Tras igualar las tarjetas, `npm test -- --watch=false`: 8 pruebas correctas; `npm run
  build`: correcto, ambos con Node 24.16.0. El build del contenedor frontend con Node
  24.19.0 también fue correcto.
- QA del catálogo con 20 tarjetas en `375x667`, `768x1024`, `1024x600` y `1440x900`:
  una única altura de tarjeta e imagen por viewport, acciones alineadas y sin
  desbordamiento horizontal.
- Corregido el listado de favoritos: el orden de la consulta JPQL se aplica al alias
  `CharacterEntity` y no al usuario raíz. Así, `GET /api/v1/users/me/favorites` vuelve
  a responder paginado y ordenado por nombre en PostgreSQL, sin el `500` de Hibernate.
- Nueva prueba de integración `FavoriteServiceIntegrationTest`: valida el listado
  paginado y el orden alfabético sobre H2 con Liquibase. Usa una base H2 aislada para
  no interferir con la prueba de consumo Kafka que comparte el perfil `test`.
- Tras la corrección, `JAVA_HOME=.../microsoft-11.jdk/Contents/Home ./mvnw -B verify`:
  10 pruebas correctas y JAR generado. Una primera repetición detectó una respuesta
  `404` transitoria en `RickMortyClientTest`; la ejecución completa posterior fue verde.
- Eliminado el autorregistro público. `POST /api/v1/admin/users` crea cuentas estándar
  y queda protegido por `ADMIN`; el catálogo y el resto de la API de aplicación exigen
  autenticación, salvo login, documentación y healthchecks operativos.
- `/login` es la única pantalla pública de la SPA. Un guard padre protege catálogo,
  detalle, favoritos, administración y 404; la pérdida o expiración local de la sesión
  fuerza la navegación al login.
- Añadida la pantalla responsive `/admin/users`, visible solo para administradores, y
  pruebas automatizadas de seguridad, guards y cliente HTTP.
- Tras el cambio de acceso, `JAVA_HOME=.../microsoft-11.jdk/Contents/Home ./mvnw -B
  verify`: 14 pruebas correctas y JAR generado. `npm test -- --watch=false`: 13 pruebas
  correctas con Node 24.16.0. `npm run build`: compilación de producción correcta.
- QA del login en `375x667`, `768x1024`, `1024x600`, `1366x768` y `1440x900`: sin
  desbordamiento horizontal; el desplazamiento vertical es natural en móvil/tablet y
  la pantalla encaja completa en los viewports de escritorio comprobados. El enlace de
  marca se corrigió de 35 px a un objetivo táctil de 44 px y se verificó en `375x667`.
- QA autenticado de `/admin/users` con administrador ficticio y H2 efímero en
  `375x667`, `768x1024`, `1024x600` y `1440x900`: sin desbordamiento horizontal;
  formularios y navegación alcanzables, scroll vertical natural cuando fue necesario.
  El objetivo táctil de `Salir` se corrigió de 37 px a 44 px y la composición de dos
  columnas se reservó para el breakpoint compartido de `62rem`, evitando una palabra
  huérfana en tableta.
- Tras esos ajustes, `npm run build` volvió a completar correctamente con Node 24.16.0
  (bundle inicial 561.05 kB). Backend, frontend y navegador temporales quedaron cerrados;
  no se creó ninguna cuenta adicional desde el formulario durante el QA.
- El cliente de Rick and Morty limita la frecuencia de solicitudes y aplica reintentos
  acotados con backoff y jitter ante `429`, `5xx` y fallos de conexión. Respeta
  `Retry-After` hasta el máximo configurable y dispone de pruebas WireMock para
  recuperación y agotamiento de intentos; su ejecución queda pendiente de autorización.
- Auditoría estática de puertos completada: documentación, valores locales del backend
  y listener externo de Kafka coinciden con los puertos publicados por Compose.
  `git diff --check` fue correcto; la validación de Compose y el arranque no se
  repitieron porque requieren autorización explícita de ejecución.
- El origen CORS del frontend publicado por Compose (`http://localhost:4400`) se añadió
  junto al origen de desarrollo `4200` mediante configuración externa tipada. La prueba
  de login comprueba el origen de Compose; su ejecución queda pendiente de autorización.

## Siguiente paso recomendado

Continuar `NEXT_FEATURES.md` con los casos restantes de tokens, favoritos, DLT,
proyección y consulta Neo4j.

## Ajustes de interfaz posteriores a la verificación

- Se corrigió el vocabulario visual que presentaba los personajes como «señales»:
  catálogo, errores, acceso y relaciones usan ahora términos propios del dominio de
  Rick and Morty. También se tradujeron etiquetas decorativas que permanecían en inglés,
  sin alterar los valores procedentes de la API externa ni los valores enviados como
  filtros. La verificación de frontend queda pendiente de autorización de ejecución.

## Riesgos abiertos

- `.nvmrc` fija Node 24.19.0, pero el ejecutable disponible en esta sesión fue 24.16.0.
  Es compatible con el mínimo `^24.15.0` y se usó para la validación responsive;
  instalar el pin del proyecto al preparar un entorno nuevo.
- Los seis servicios de Compose se validaron sobre los volúmenes locales existentes;
  aún falta repetir el escenario integral desde una base limpia.
- Seguridad, favoritos, DLT, Neo4j, guards e interceptor están implementados, pero aún
  no cuentan con pruebas automatizadas específicas suficientes para cerrar sus puertas.
- OpenAPI declara `opaqueBearer` en las operaciones protegidas, pero todavía no detalla
  todos los códigos de error por operación.
