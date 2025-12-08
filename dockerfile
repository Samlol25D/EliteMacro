# Dockerfile CORREGIDO
# Etapa 1: Construcción - USAR JAVA 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código y compilar SIN PERFIL PROD
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución - USAR JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Variables para Render
ENV PORT=8080
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Copiar JAR
COPY --from=builder /app/target/*.jar app.jar

# Puerto
EXPOSE ${PORT}

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]