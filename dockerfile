# Dockerfile EMERGENCIA
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# DEBUG: Mostrar variables
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx256m -Xms128m -Dserver.port=${PORT} -Dspring.datasource.driver-class-name=org.postgresql.Driver"

# Forzar driver PostgreSQL
RUN echo "Forcing PostgreSQL driver..."

COPY --from=builder /app/target/*.jar app.jar

EXPOSE ${PORT}

# Comando con debug
CMD echo "=== DEBUG INFO ===" && \
    echo "PORT: $PORT" && \
    echo "DATABASE_URL: $DATABASE_URL" && \
    echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE" && \
    echo "Starting app..." && \
    java ${JAVA_OPTS} -jar app.jar