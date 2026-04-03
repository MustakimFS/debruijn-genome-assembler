# Stage 1 - Build both JARs
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy root pom and src (genome-toolkit)
COPY pom.xml .
COPY src ./src
COPY lib ./lib
COPY data ./data

# Build the toolkit JAR first
RUN mvn clean package -DskipTests

# Manually install toolkit JAR into local Maven repo
RUN mvn install:install-file \
    -Dfile=lib/genome-toolkit-1.0.0.jar \
    -DgroupId=com.genome \
    -DartifactId=genome-toolkit \
    -Dversion=1.0.0 \
    -Dpackaging=jar

# Copy backend and build it
COPY backend ./backend
RUN mvn -f backend/pom.xml clean package -DskipTests

# Stage 2 - Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/backend/target/genome-assembler-api-1.0.0.jar app.jar
COPY --from=build /app/data ./data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]