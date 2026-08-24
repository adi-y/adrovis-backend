# syntax=docker/dockerfile:1.4

# Build stage
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

# Use a mirror for Central to avoid 429 rate limiting on the direct repo
COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B dependency:go-offline

COPY src src

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]