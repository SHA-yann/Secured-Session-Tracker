# Stage 1 : Build the application
FROM maven:3.9.10-openjdk-17 AS build
WORKDIR /api
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 : Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /api
RUN addgroup -S Dev && adduser -S dev -G dev
USER dev:dev
COPY --from=build /api/target/*.jar api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api.jar"]