# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom and build core assembler first
COPY ../pom.xml /app/parent-pom.xml
COPY ../src /app/src
WORKDIR /app
RUN mvn clean package -DskipTests

# Copy backend code
WORKDIR /app/backend
COPY pom.xml .
COPY src ./src

# Build backend
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]