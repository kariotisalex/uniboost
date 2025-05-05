# --- STAGE 1: Build the JAR ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# --- STAGE 2: Run the app ---
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the JAR from stage 1
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
