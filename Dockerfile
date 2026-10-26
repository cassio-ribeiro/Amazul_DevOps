# Dockerfile da API Núcleo (Java 25 + Spring Boot 4.1.1)
# Baseado no exemplo multi-stage da Aula 10 (Dockerfile) - Prof. João Menk
# Única mudança em relação à aula: a versão do Java (17 -> 25), que é a do pom.xml do projeto

# Estágio 1: build da aplicação com Maven
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copia o pom.xml primeiro e baixa as dependências (aproveita o cache de camadas)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: imagem enxuta só para execução
FROM eclipse-temurin:25-jre

# Usuário não-root (requisito de segurança da disciplina)
RUN addgroup --system appuser && adduser --system --ingroup appuser appuser

WORKDIR /app

# Copia só o .jar final do estágio anterior
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser

# Porta da aplicação (server.port=8080 no application.properties)
# EXPOSE só documenta; quem publica a porta é o -p do docker run
EXPOSE 8080

# Perfil oracle: a API usa o Oracle da FIAP. As credenciais NÃO ficam na imagem,
# entram no docker run pelo --env-file .env (DB_URL, DB_USERNAME, DB_PASSWORD)
ENV SPRING_PROFILES_ACTIVE=oracle

ENTRYPOINT ["java", "-jar", "app.jar"]
