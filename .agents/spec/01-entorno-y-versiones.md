# Entorno y versiones (fijadas por la prueba)

| Elemento | Tecnología | Observación |
|---|---|---|
| Plataforma | JDK 11 | Compilación y ejecución fijadas a 11 |
| Framework | Spring Boot · Spring MVC | Rama 2.7.x (última compatible con JDK 11) |
| Relacional | PostgreSQL 10 | Atributos de entidades y funcionalidad de usuario |
| Grafo | Neo4j | Relaciones entre entidades sincronizadas |
| Mensajería | Apache Kafka 2.12-2.0.1 | Canal entre descarga y procesado |
| Frontend | Angular · Bootstrap | HTML5, CSS3, JavaScript/TypeScript |

- Toda la infraestructura declarada en un `docker-compose` con **versiones fijadas**.
- Se valora que las versiones de librerías sean **coherentes con JDK 11 y con el broker indicado**.

Versiones concretas elegidas y su justificación: [references/stack-versiones.md](../references/stack-versiones.md).
