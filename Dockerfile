# ==============================================================================
# Etapa 1: Build da Aplicacao Spring Boot com Maven
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copia pom.xml e faz download antecipado das dependencias (cache de camadas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o codigo fonte e compila o pacote JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==============================================================================
# Etapa 2: Imagem Final de Execucao (Runtime Leve)
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Instala curl para checagem de integridade (healthcheck)
RUN apk add --no-cache curl

# Cria usuario nao-root para seguranca da execucao
RUN addgroup -S sivegroup && adduser -S siveuser -G sivegroup
USER siveuser:sivegroup

# Copia o artefato .jar gerado na etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Fly.io define a porta padrao via variavel PORT (fallback 8080)
ENV PORT=8080
EXPOSE 8080

# Inicia a aplicacao passando o profile e a porta dinamicamente, limitando a memoria da JVM
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8080} -jar app.jar"]
