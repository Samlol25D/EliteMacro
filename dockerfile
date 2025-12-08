# Dockerfile - VERSIÓN FINAL CORREGIDA
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# 1. Copiar solo pom.xml primero (para cache de dependencias)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiar código fuente
COPY src ./src

# 3. COMPILAR SIN -Pprod (¡ESTO ES LO MÁS IMPORTANTE!)
RUN mvn clean package -DskipTests

# 4. Imagen de ejecución
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV PORT=8080
ENV JAVA_OPTS="-Xmx256m -Xms128m"

COPY --from=builder /app/target/*.jar app.jar

EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]