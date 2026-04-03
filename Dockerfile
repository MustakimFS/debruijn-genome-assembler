# Stage 1 - Build both JARs
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy root pom and src (genome-toolkit)
COPY pom.xml .
COPY src ./src
COPY lib ./lib

# Build the toolkit JAR first
RUN mvn clean package -DskipTests

# Copy backend and build it
COPY backend ./backend
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 2 - Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/target/genome-assembler-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]