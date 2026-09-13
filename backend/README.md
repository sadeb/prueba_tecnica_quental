# Backend Spring Boot

Servicio Java 11 construido con Maven y Spring Boot 2.7.18. Expone la API propia, sincroniza Rick and Morty mediante Kafka y persiste atributos en PostgreSQL y relaciones en Neo4j.

## Desarrollo

```bash
./mvnw test
./mvnw spring-boot:run
```

La aplicación espera PostgreSQL, Kafka y Neo4j. La configuración se obtiene de variables de entorno; consulta `application.yml` y el `.env.example` raíz.

Liquibase es el único propietario del esquema relacional. Hibernate está configurado con `ddl-auto: validate`.

La descarga desde la API externa limita la frecuencia de solicitudes y reintenta de
forma acotada respuestas `429`, errores `5xx` y fallos de conexión. Se puede ajustar
con `EXTERNAL_REQUEST_INTERVAL`, `EXTERNAL_MAX_ATTEMPTS`,
`EXTERNAL_INITIAL_BACKOFF` y `EXTERNAL_MAX_BACKOFF`; el cliente respeta `Retry-After`
hasta el máximo configurado.

El único endpoint público de la aplicación es `POST /api/v1/auth/login`. Las cuentas
estándar se crean con `POST /api/v1/admin/users`, disponible exclusivamente para una
sesión con rol `ADMIN`; no existe autorregistro público.
