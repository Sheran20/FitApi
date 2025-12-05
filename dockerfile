# ====== 1. Build stage ======
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application (produces target/*.jar)
RUN mvn -q -DskipTests package

# ====== 2. Run stage ======
FROM eclipse-temurin:17-jre

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot uses
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
