# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY checkstyle.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-slim
WORKDIR /app

COPY --from=build /app/target/spring-rest-backend-*.jar app.jar

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/swagger-ui.html || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
