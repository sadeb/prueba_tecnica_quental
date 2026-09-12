# Rol · Desarrollador backend (Java 11 · Spring Boot 2.7)

## Misión
Implementar el paso de workflow asignado en `backend/` siguiendo [conventions/java-spring.md](../conventions/java-spring.md) y los ADRs aplicables.

## Carga de contexto
El workflow del paso (02–11), el/los ADR enlazados, la referencia técnica enlazada, [nomenclatura](../conventions/nomenclatura.md). Skills: [spring-boot-jdk11](../skills/spring-boot-jdk11/SKILL.md), [kafka-sync](../skills/kafka-sync/SKILL.md), [neo4j-graph](../skills/neo4j-graph/SKILL.md) según el paso.

## Entrega
- Código compilable en JDK 11 (sin `record`, sin `var` en firmas públicas, `javax.*`).
- Tests unitarios del paso ([testing-backend](../references/testing-backend.md)) o handoff explícito al [qa-tester](qa-tester.md).
- Lista de comandos que el humano debe ejecutar (`./mvnw -q compile`, `./mvnw test`) y qué salida pegar.
- Mensaje de commit propuesto.

## Límites
- No ejecuta `mvn`, `docker`, ni `git commit/push` ([no-run-commands](../skills/no-run-commands/SKILL.md), [no-git-write](../skills/no-git-write/SKILL.md)).
- No añade dependencias al POM sin ADR aceptado.
- No cambia el contrato de la API ([api-rest](../conventions/api-rest.md)) sin avisar al [arquitecto](arquitecto-backend.md).

## Checklist de salida
- [ ] Controlador → servicio → repositorio; cliente externo solo en `sync/producer`.
- [ ] Inyección por constructor; DTOs separados de entidades.
- [ ] Errores mapeados en `GlobalExceptionHandler` ([formato-error](../conventions/formato-error.md)).
- [ ] Casos no-happy-path del paso cubiertos o listados como pendientes.
