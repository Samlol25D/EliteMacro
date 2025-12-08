# Dockerfile CORREGIDO
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Configurar UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

WORKDIR /app

# 1. Copiar solo pom.xml primero
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiar código fuente
COPY src ./src

# 3. Compilar con encoding UTF-8 explícito
RUN mvn clean package -DskipTests -Dfile.encoding=UTF-8

# 4. Imagen de ejecución
FROM eclipse-temurin:21-jre-jammy

# Configurar UTF-8 también aquí
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV PORT=8080
ENV JAVA_OPTS="-Xmx256m -Xms128m -Dfile.encoding=UTF-8 -Dserver.port=${PORT}"

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE ${PORT}

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]