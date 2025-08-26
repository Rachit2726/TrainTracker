# Use Eclipse Temurin Java 17 base image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy Maven wrapper and pom.xml first for caching dependencies
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download all dependencies (offline mode) to speed up build
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src ./src

# Build the project, skip tests for faster build
RUN ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the Spring Boot jar
CMD ["java", "-jar", "target/statustracker-0.0.1-SNAPSHOT.jar"]
