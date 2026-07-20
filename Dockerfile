FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests \
    && cp "$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | head -n 1)" /workspace/app.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV TZ=Asia/Taipei

COPY --from=build /workspace/app.jar /app/app.jar

COPY --from=build \
    /workspace/src/main/resources/static/images \
    /app/src/main/resources/static/images

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
