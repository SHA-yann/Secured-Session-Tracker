# STEP 1: Application Build (Maven + OpenJDK 17)
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Set the working directory for the compilation phase
WORKDIR /app

# Copy the project descriptor file for dependency resolution
COPY pom.xml .

# Copy the application source code
COPY src ./src

# Build the application and package the executable JAR (skipping unit tests)
RUN mvn clean package -DskipTests


# STEP 2: Production Runtime Image (Minimal JRE 17)
FROM eclipse-temurin:17-alpine

# Configure JVM flags tailored for container environments:
# - UseContainerSupport: Adjusts CPU/RAM allocation to Docker/Cgroups resource limits
# - MaxRAMPercentage: Allocates up to 75% of container RAM to the JVM Heap
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Create a dedicated system group and non-root user for enhanced security
RUN addgroup -S springgroup && adduser -S springuser -G springgroup

# Set the application working directory
WORKDIR /app

# Switch to the non-privileged user for remaining execution steps
USER springuser:springgroup

# Copy the executable JAR artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default HTTP listening port for the Spring Boot service
EXPOSE 8080

# Set the container entrypoint, dynamically evaluating JVM environment flags
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]