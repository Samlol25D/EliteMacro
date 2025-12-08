# Etapa 1: Construcción
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar pom.xml y descargar dependencias (cache eficiente)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código y compilar
COPY src ./src
RUN mvn clean package -DskipTests -Pprod

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Variables para Render
ENV PORT=8080
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Copiar JAR
COPY --from=builder /app/target/*.jar app.jar

# Puerto (Render inyecta variable PORT)
EXPOSE ${PORT}

# Health check para Render
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]