# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port that Spring Boot runs on
# Render typically uses port 10000, but this can be overridden
EXPOSE 8081

# Set JVM options for containerized environments
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
# The PORT environment variable from Render will override the application.properties port
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8081} -jar app.jar"]
