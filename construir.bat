@echo off
echo =========================================
echo INICIANDO COMPILACION SECUENCIAL (1x1)
echo =========================================
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

echo =========================================
echo COMPILACION TERMINADA
echo Levantando el entorno en segundo plano...
echo =========================================

echo =========================================
echo FASE 1: Levantando los microservicios base
echo =========================================
:: Al NO usar el perfil test-suite aqui, solo levanta las apps normales
docker compose up -d

echo.
echo =========================================
echo Esperando a que Spring Boot y Eureka esten listos...
echo (Esperando 300 segundos)
echo =========================================
timeout /t 300 /nobreak

echo.
echo =========================================
echo FASE 2: Levantando y ejecutando los tests
echo =========================================
:: Ahora SI llamamos al perfil test-suite. Docker detectara que las apps
:: ya estan corriendo y solo encendera los contenedores de prueba.
docker compose --profile test-suite up

echo.
echo =========================================
echo FLUJO COMPLETADO.
echo =========================================
pause