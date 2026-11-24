# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

# Maven's local repo lives in a persistent BuildKit cache, so a pom.xml change
# only downloads the new/changed dependencies instead of everything again.
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
