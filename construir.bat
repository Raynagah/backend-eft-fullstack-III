@echo off
setlocal enabledelayedexpansion
echo ===================================================
echo INICIANDO CONFIGURACION Y COMPILACION SECUENCIAL (1x1)
echo ===================================================
echo.

echo [Fase Inicial] Limpiando contenedores y volumenes previos...
:: -v destruye el volumen pg_data_local para forzar la ejecucion de init-dbs.sql
docker compose down -v
echo.

echo [1/8] Construyendo Eureka Server...
docker compose build eureka-server

echo [2/8] Construyendo API Gateway...
docker compose build api-gateway

echo [3/8] Construyendo MS Usuarios...
docker compose build ms-usuarios

echo [4/8] Construyendo MS Gestion Mascotas...
docker compose build ms-gestion-mascotas

echo [5/8] Construyendo MS Geolocalizacion...
docker compose build ms-geolocalizacion

echo [6/8] Construyendo MS Motor Coincidencias...
docker compose build ms-motor-coincidencias

echo [7/8] Construyendo MS Notificaciones...
docker compose build ms-notificaciones

echo [8/8] Construyendo MS BFF...
docker compose build ms-bff
echo.

echo ===================================================
echo FASE 1: Levantando Base de Datos e Infraestructura
echo ===================================================
:: Levantamos solo la base de datos primero para asegurar su inicializacion pura
docker compose up -d postgres-db
echo Esperando a que PostgreSQL procese init-dbs.sql...
echo.

:: Levantamos el resto de servicios base de la aplicacion
docker compose up -d eureka-server api-gateway ms-gestion-mascotas ms-geolocalizacion ms-motor-coincidencias ms-usuarios ms-notificaciones ms-bff

echo.
echo ===================================================
echo Esperando a que Spring Boot y Eureka esten listos...
echo (Contador de estabilidad: 45 segundos)
echo ===================================================
timeout /t 45 /nobreak

echo.
echo ===================================================
echo FASE 2: Levantando y ejecutando la Suite de Tests
echo ===================================================
:: Llama al perfil test-suite para los contenedores de pruebas unitarias/integracion
docker compose --profile test-suite up

echo.
echo ===================================================
echo FLUJO COMPLETADO EXITOSAMENTE
echo ===================================================
pause