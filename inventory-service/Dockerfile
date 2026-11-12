# syntax=docker/dockerfile:1

# STAGE 1: BUILD Docker Image
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl inventory-service -am package -DskipTests

# STAGE 2: Run Docker
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/inventory-service/target/inventory-service.jar app.jar

USER app
EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -q0- http://localhost:8082/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]