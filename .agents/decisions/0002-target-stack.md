# ADR 0002: Stack técnico objetivo

- Estado: Implementado; validación integral de Compose pendiente
- Fecha: 2026-09-11
- Alcance de implementación: Fases 1 a 8

## Contexto

La prueba exige JDK 11, Spring Boot 2.7.x, Spring MVC, PostgreSQL 10, Neo4j, Kafka 2.12-2.0.1, Angular, Bootstrap y Docker Compose. El usuario añadió Maven, Liquibase y el uso explícito de JPA/Hibernate.

## Decisión

- Backend con Java 11, Apache Maven 3.9.11 ejecutado mediante Maven Wrapper y Spring Boot 2.7.18.
- API síncrona con Spring MVC, Bean Validation y Spring Security.
- Persistencia relacional con Spring Data JPA y Hibernate administrado por Spring Boot.
- Liquibase será el único mecanismo de creación y evolución del esquema; Hibernate validará, no generará, el esquema.
- PostgreSQL 10 será la fuente de verdad y Neo4j 4.4 mantendrá una proyección de relaciones.
- Spring Kafka usará la versión gestionada por Boot 2.7 y solo funciones compatibles con broker Kafka 2.0.1.
- SPA Angular en proyecto independiente, con versión estable fijada al autorizar su scaffold y Bootstrap 5.
- Docker Compose final contendrá aplicaciones e infraestructura con versiones fijadas.

## Consecuencias

- No se emplearán APIs de Spring Boot 3/4, `jakarta.*`, Hibernate 6 ni versiones incompatibles de Neo4j.
- El BOM de Spring Boot gobernará versiones siempre que sea posible.
- Cada actualización fuera de estas líneas exige una prueba de compatibilidad y un nuevo ADR.
- El cierre requiere comprobar el arranque conjunto con las imágenes fijadas y no solo la
  compatibilidad declarada.
