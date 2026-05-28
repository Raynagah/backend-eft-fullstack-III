# 🐾 Sistema de Gestión de perdidas y encuentros de Mascotas - Fullstack III

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Este repositorio contiene la arquitectura backend orientada a microservicios para la plataforma de gestión de perdidas y encuentros, geolocalización y coincidencias (*match*) de mascotas.

---

## 🔗 Enlaces Rápidos

- **Repositorio Principal (Backend):**  
  [EFT-Desarrollo-Fullstack-III](https://github.com/Raynagah/EFT-Desarrollo-Fullstack-III.git)

- **Repositorio Frontend:**  
  [frontend-eft-fullstack-III](https://github.com/Raynagah/frontend-eft-fullstack-III.git)

---

# 📂 Estructura del Proyecto

El proyecto sigue una arquitectura de microservicios, donde cada dominio tiene su propio directorio con su código fuente, dependencias (`pom.xml`) y configuración de despliegue (`Dockerfile`).

```text
backend-eft-fullstack-III/
├── api-gateway/            # Puerta de enlace, enrutamiento y seguridad
├── bff/                    # Backend For Frontend (Agregador para la UI)
├── eureka-server/          # Servidor de descubrimiento (Service Registry)
├── gestion-mascotas/       # Microservicio principal (CRUD y catálogo)
├── ms-geolocalizacion/     # Microservicio de ubicación geográfica y distancias
├── ms-motor-coincidencias/ # Microservicio de Match de mascotas perdidas/encontradas
├── notificaciones/         # Microservicio de correos y sistema de alertas
├── usuarios/               # Microservicio de identidad, registro y seguridad JWT
├── docker-compose.yml      # Orquestador principal de la infraestructura y pruebas
├── init-dbs.sql            # Script de inicialización de los esquemas de base de datos
└── README.md               # Documentación del proyecto
```

---

# 🏛️ Ecosistema de Microservicios

| Servicio | Puerto | Descripción |
|---|---|---|
| **Eureka Server** | `8761` | Actúa como el directorio del sistema. Todos los microservicios se registran aquí para poder encontrarse entre sí dinámicamente sin depender de IPs estáticas. |
| **API Gateway** | `8080` | Punto de entrada único público. Recibe las peticiones del exterior, aplica filtros de seguridad (CORS, validaciones) y las enruta al servicio interno correspondiente. |
| **BFF** | `8087` | Adaptador específico para la interfaz de usuario. En lugar de que el frontend llame a 5 servicios distintos, llama al BFF, el cual orquesta y junta los datos en una sola respuesta. |
| **Mascotas** | `8081` | Administra el catálogo completo, permitiendo registrar mascotas perdidas, encontradas. |
| **Geolocalización** | `8083` | Calcula distancias y maneja las coordenadas (latitud/longitud) de los reportes. |
| **Coincidencias** | `8084` | El cerebro del sistema. Analiza características y distancias para sugerir si una mascota encontrada es la misma que alguien reportó como perdida. |
| **Usuarios** | `8085` | Gestiona el registro, autenticación (generación de tokens JWT) y perfiles de los dueños/adoptantes. |
| **Notificaciones** | `8086` | Escucha eventos del sistema y despacha alertas automáticas (ej. cuando hay un nuevo match). |

---

# 🐳 Infraestructura como Código (Archivos Clave)

## 1. Dockerfile (Multi-stage Build optimizado)

Cada microservicio utiliza un Dockerfile dividido en dos etapas para mantener las imágenes de producción seguras y ultraligeras:

### 🔹 Etapa 1 — Compilación

- Usa `maven:3.8.5-openjdk-17-slim`
- Aprovecha la caché de Docker BuildKit:
  ```bash
  --mount=type=cache,target=/root/.m2
  ```
- Descarga únicamente las dependencias faltantes
- Evita problemas de `go-offline`
- Genera rápidamente el `.jar`

### 🔹 Etapa 2 — Ejecución

- Usa `eclipse-temurin:17-jre-alpine`
- Copia únicamente el `.jar`
- Ejecuta la aplicación con:
  ```bash
  java -jar
  ```
- Reduce drásticamente el tamaño de la imagen final

### 📄 Dockerfile

```dockerfile
# --- ETAPA 1: Compilación ---
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

# Copiamos todo el proyecto directamente
COPY pom.xml .
COPY src ./src

# Compilamos usando la caché nativa de Docker para la carpeta .m2
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# --- ETAPA 2: Ejecución ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## 2. docker-compose.yml

El director de orquesta que levanta todo el entorno estructurado de la siguiente forma:

### 🔹 Servicios Base

- Configura PostgreSQL
- Inyecta variables de entorno
- Define URLs de Eureka
- Establece orden de inicio seguro usando:

```yaml
depends_on:
  service_healthy
```

### 🔹 Perfil de Pruebas (`test-suite`)

Incluye contenedores paralelos (`*-test`) que:

- Montan el código local en vivo
- Ejecutan:
  ```bash
  mvn clean test
  ```
- Generan reportes de cobertura JaCoCo
- Automatizan pruebas aisladas

---

## 3. init-dbs.sql

Script que se inyecta automáticamente en PostgreSQL durante la inicialización.

### Funciones principales

- Crear bases de datos independientes:
  - `db_usuarios`
  - `db_mascotas`
  - `db_geolocalizacion`
  - etc.

### Beneficios

- Aislamiento total entre microservicios
- Independencia de datos
- Escalabilidad modular

---

# 🚀 Instalación y Despliegue

Asegúrate de tener Docker Desktop iniciado y los puertos libres.

Ejecuta los siguientes comandos desde la raíz del proyecto.

---

## 🔹 Opción A — Ejecutar solo microservicios (Modo Desarrollo)

Compila y levanta toda la infraestructura:

```bash
docker compose up --build
```

---

## 🔹 Opción B — Ejecutar microservicios + Suite de Pruebas

Levanta el entorno completo junto a los contenedores de prueba automatizados:

```bash
docker compose --profile test-suite up --build
```

Si se agregan test ejecutar:

```bash
docker-compose --profile reportes up generar-dashboard
```

---

# 🛠️ Resolución de Problemas (Troubleshooting)

Si trabajas en Windows + WSL y experimentas bloqueos o congelamientos por alto consumo de Docker, utiliza los siguientes comandos.

---

## 1. Cerrar Docker Desktop forzadamente

```dos
taskkill /IM "Docker Desktop.exe" /F
```

---

## 2. Apagar completamente WSL

```dos
wsl --shutdown
```

---

## 3. Solucionar errores Maven (`zip file is empty`)

Si cancelaste una ejecución y Maven quedó corrupto en caché:

1.- Bajar contenedores

```bash
docker compose down
```

2.- Limpiar contenedores

```bash
docker container prune -f
```

3.- Eliminar el volumen de cache

```bash
docker volume rm backend-eft-fullstack-iii_maven_cache
```

Luego:

1. Reinicia Docker Desktop
2. Espera a que cargue correctamente
3. Ejecuta nuevamente el despliegue

---

# ✅ Tecnologías Utilizadas

- Java 17
- Spring Boot 3
- Spring Cloud
- Eureka Server
- API Gateway
- PostgreSQL 15
- Docker & Docker Compose
- JWT Authentication
- Maven
- JaCoCo
- WSL2

---

# 📌 Arquitectura General

```text
Frontend
   │
   ▼
  BFF
   │
   ▼
API Gateway
   │
   ├── Usuarios
   ├── Mascotas
   ├── Geolocalización
   ├── Coincidencias
   └── Notificaciones

Todos los servicios registrados en:
Eureka Server
```

---

# 📄 Licencia

Proyecto desarrollado con fines académicos para la asignatura **Desarrollo Fullstack III**.
