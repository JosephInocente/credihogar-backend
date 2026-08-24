# Usamos Java 21 (que es la versión que vi que usas en tu Eclipse)
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copiamos todo el código de tu proyecto al contenedor
COPY . .

# Le damos permiso al instalador y compilamos el proyecto
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Exponemos el puerto 8080 que usa Spring Boot
EXPOSE 8080

# Encendemos el sistema
ENTRYPOINT ["java", "-jar", "target/sistema-0.0.1-SNAPSHOT.jar"]