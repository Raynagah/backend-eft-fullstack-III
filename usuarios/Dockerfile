# --- ETAPA 1: Compilación ---
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copiamos todo el proyecto directamente
COPY pom.xml .
COPY src ./src

# Compilamos usando la caché nativa de Docker para la carpeta .m2
# Esto descarga solo lo que falta y nunca fallará por el bug de go-offline
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]