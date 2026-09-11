# Backend Spring Boot

Servicio Java 11 construido con Maven y Spring Boot 2.7.18. Expone la API propia, sincroniza Rick and Morty mediante Kafka y persiste atributos en PostgreSQL y relaciones en Neo4j.

## Desarrollo

```bash
./mvnw test
./mvnw spring-boot:run
```

La aplicación espera PostgreSQL, Kafka y Neo4j. La configuración se obtiene de variables de entorno; consulta `application.yml` y el `.env.example` raíz.

Liquibase es el único propietario del esquema relacional. Hibernate está configurado con `ddl-auto: validate`.
