# Usamos JRE (solo lo necesario para ejecutar) y Alpine (Linux súper ligero)
FROM eclipse-temurin:17-jre-alpine

# Mantenemos tu buena práctica de usar una carpeta específica
WORKDIR /app

# Copiamos el ejecutable
COPY target/*.jar app.jar

# Ejecutamos leyendo los límites de RAM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]