FROM maven:3.9.11-eclipse-temurin-11 AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -DskipTests dependency:go-offline
COPY src src
# RUN ./mvnw -B -DskipTests package
RUN ./mvnw -B package

FROM eclipse-temurin:11-jre-jammy
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring
WORKDIR /app
COPY --from=build --chown=spring:spring /workspace/target/rick-morty-api-*.jar app.jar
USER spring
EXPOSE 8989
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
