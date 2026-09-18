FROM maven:3.9.15-eclipse-temurin-26 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S nestsync && adduser -S nestsync -G nestsync
WORKDIR /app
COPY --from=build --chown=nestsync:nestsync /app/target/nestsync-backend-*.jar app.jar
USER nestsync
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 CMD wget -q --spider http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
