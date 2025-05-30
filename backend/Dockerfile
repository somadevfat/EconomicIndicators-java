# 1) Build stage
# Use Maven 3.9.9 and Java 17 (Eclipse Temurin) on Ubuntu Focal
FROM maven:3.9.9-eclipse-temurin-17-focal AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2) Runtime stage
# Use Java 17 JRE (Eclipse Temurin) on Ubuntu Focal for the runtime
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]