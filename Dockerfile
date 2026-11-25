# syntax=docker/dockerfile:1
# Multi-stage build: backend fat jar (webapp resources are bundled inside it)
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY webapp/pom.xml webapp/pom.xml
COPY android/pom.xml android/pom.xml
COPY backend/src backend/src
COPY webapp/src webapp/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -pl webapp,backend -am -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /src/backend/target/backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
# JAVA_OPTS is honored by the entrypoint; set SPRING_PROFILES_ACTIVE=prod + env config in prod
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
