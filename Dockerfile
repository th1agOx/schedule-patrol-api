# syntax=docker/dockerfile:1

# Build: compila o .jar com o Maven e o JDK 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# O cache do Maven fica em um mount do BuildKit: sobrevive entre builds
# (mesmo quando o pom.xml muda) e evita baixar as dependências de novo.
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests

# Execução: só o JRE e o .jar, imagem menor e com menos superfície de ataque.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuário sem privilégios: a aplicação não roda como root dentro do container.
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
