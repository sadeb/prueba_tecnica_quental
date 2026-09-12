---
name: testing-aislado
description: Procedimiento para escribir pruebas backend (JUnit 5, MockMvc, MockRestServiceServer, EmbeddedKafka, H2) y frontend (HttpTestingController, guardas, componentes) sin red ni infraestructura manual, priorizando casos límite. Cargar al escribir cualquier test.
---

# testing-aislado

## Antes de escribir
[references/testing-backend.md](../../references/testing-backend.md) o [references/testing-frontend.md](../../references/testing-frontend.md). Requisito: [spec/07](../../spec/07-pruebas.md).

## Procedimiento
1. **Tabla de casos primero** (given / when / then), con al menos un caso límite por comportamiento. Pegarla en la respuesta antes del código.
2. Elegir el nivel más bajo que pruebe el comportamiento (unitario > slice > SpringBootTest).
3. Un test = una aserción de comportamiento; nombre `should<Comportamiento>When<Condicion>` o `given_when_then`.
4. Datos de prueba mínimos y explícitos; sin fixtures gigantes. Un `TestData` con fábricas (`aCharacter()`, `anExternalCharacterJson()`).
5. Sin `Thread.sleep`; en Kafka usar `Awaitility` **no** (dependencia extra): usar `ConsumerRecord` con `KafkaTestUtils.getSingleRecord` o `CountDownLatch` en un listener de test.

## Casos límite obligatorios por módulo
- Cliente externo: 404, 500, timeout, JSON sin `results`, `origin.url == ""`, URL de episodio no numérica.
- Consumidor: mensaje duplicado, `schemaVersion` desconocido, JSON inválido → DLT, referencia a episodio desconocido → placeholder.
- Auth: token caducado, firma alterada, cabecera ausente, usuario duplicado en registro.
- Favoritos: añadir dos veces, eliminar inexistente, personaje inexistente.
- API consulta: filtro inválido → 400, página fuera de rango → lista vacía 200, id inexistente → 404.
- Frontend: guarda sin sesión → `/login` con `returnUrl`; interceptor 401 → logout; lista con filtro → params correctos y `page=0`; estado `error` renderiza alerta.

## Prohibido
Red real; Docker obligatorio para `mvn test`; tests de getters; mocks de la clase bajo prueba; `@SpringBootTest` donde baste `@WebMvcTest`.

## Entrega
Tabla de casos, ficheros de test, comando para el humano con filtro de test (`./mvnw test -Dtest=SyncFlowIT`, `npx ng test --watch=false --include='**/auth.guard.spec.ts'`) y qué líneas pegar ([no-run-commands](../no-run-commands/SKILL.md)).
