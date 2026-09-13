# Build: Maven + JDK 11 (imagen oficial, multi-arch). No se usa ./mvnw: el .gitignore raiz
# ignora *.jar (maven-wrapper.jar) y la imagen fija Maven de forma explicita.
FROM maven:3.9.16-eclipse-temurin-11 AS build
WORKDIR /workspace

# Capa de dependencias: solo se invalida si cambia el pom. El cache mount de ~/.m2
# persiste entre builds (BuildKit).
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

# Tests: se ejecutan con ./mvnw en local/CI, no en el build de imagen.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -Dmaven.test.skip=true package \
 && java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Runtime: JRE 11 Temurin (jammy). 11-jre-alpine es solo amd64 -> descartado en Apple Silicon.
FROM eclipse-temurin:11.0.31_11-jre-jammy

RUN useradd --system --uid 10001 --user-group --no-create-home \
    --home-dir /app --shell /usr/sbin/nologin spring

WORKDIR /app
# Orden de capas: de menos a mas volatil (mejor reutilizacion de cache).
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

# Perfil demo: heap 50% del limite del contenedor (deja sitio a metaspace/threads/buffers),
# Serial GC y solo C1 (arranque rapido, menos RSS). Sobrescribible desde compose.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080
USER spring
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
