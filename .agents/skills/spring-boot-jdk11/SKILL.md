---
name: spring-boot-jdk11
description: Procedimiento para escribir código Java 11 con Spring Boot 2.7 en este proyecto: restricciones de lenguaje, starters, estructura de paquetes, manejo de errores y configuración. Cargar antes de tocar cualquier fichero de projects/backend/.
---

# spring-boot-jdk11

## Antes de escribir
Leer [conventions/java-spring.md](../../conventions/java-spring.md) y [references/spring-boot-2.7.md](../../references/spring-boot-2.7.md). Comprobar versiones en [references/stack-versiones.md](../../references/stack-versiones.md).

## Checklist de lenguaje (JDK 11)
- Sin `record`, sin `sealed`, sin `switch` con flechas ni `instanceof` con patrón, sin text blocks (`"""`). `var` solo en variables locales.
- `Optional` en retornos de repositorio; nunca como parámetro ni campo.
- `java.time` (`Instant`, `OffsetDateTime`) para fechas; `air_date` se guarda como `String`.
- Inmutabilidad: clases `final`, campos `private final`, constructor completo, sin setters en DTOs de respuesta.

## Checklist de Spring
- `javax.persistence`, `javax.validation`, `javax.servlet`.
- Un `@RestController` por recurso; `@RequestMapping("/api/<recurso>")`.
- `@Transactional` en servicios; `readOnly = true` en lecturas.
- Propiedades en `@ConfigurationProperties(prefix = "...")` + `@Validated`; registrar con `@EnableConfigurationProperties`.
- Excepciones → `GlobalExceptionHandler` ([formato-error](../../conventions/formato-error.md)); no capturar `Exception` genérica en servicios.
- Logs SLF4J con parámetros `{}`; sin concatenación.

## Al añadir un endpoint
1. DTO `Request` con validaciones y DTO `Response` inmutable.
2. Servicio con la lógica; repositorio si hace falta.
3. Controlador con anotaciones OpenAPI mínimas ([references/openapi-springdoc.md](../../references/openapi-springdoc.md)).
4. Fila en [conventions/api-rest.md](../../conventions/api-rest.md) si el contrato cambia.
5. Test `@WebMvcTest` de códigos de estado y validación ([testing-aislado](../testing-aislado/SKILL.md)).

## Entrega
Ficheros tocados, comando para el humano (`./mvnw -q -DskipTests compile` o `./mvnw test -Dtest=X`) y commit propuesto. No ejecutar ([no-run-commands](../no-run-commands/SKILL.md)).
