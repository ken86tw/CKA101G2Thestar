# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Copy build metadata first so dependencies remain cached when source files change.
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn/ .mvn/
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw \
    && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -B -DskipTests \
    && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.jar.original' -print -quit)" /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app --shell /usr/sbin/nologin spring

WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/app.jar ./app.jar

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
