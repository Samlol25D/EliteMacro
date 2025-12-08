# Dockerfile CON YAML
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx256m -Xms128m -Dserver.port=${PORT}"
COPY --from=builder /app/target/*.jar app.jar
EXPOSE ${PORT}
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]