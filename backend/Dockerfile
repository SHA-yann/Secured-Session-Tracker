# Stage 1 : Build the application
FROM maven:3.9-eclipse-temurin-17-alpine AS build
RUN addgroup -S springgroup && adduser -S springuser -G springgroup
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 : Run the application
FROM eclipse-temurin:17-jre-alpine
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
RUN addgroup -S springgroup && adduser -S springuser -G springgroup
WORKDIR /app
USER springuser:springgroup
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]