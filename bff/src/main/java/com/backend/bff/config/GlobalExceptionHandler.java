package com.backend.bff.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de caídas de otros microservicios (Mascotas o Geo)
    @ExceptionHandler(feign.FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeignException(feign.FeignException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Servicio temporalmente no disponible");
        // Hacemos el mensaje un poco más genérico para que aplique a cualquier MS
        error.put("mensaje", "Estamos teniendo problemas para procesar la información en nuestros servidores internos :(");
        // Opcional: imprimir el error real en consola para ti
        System.err.println("Error de Feign: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // 2. Manejo de cualquier otro error inesperado (Catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor");
        error.put("mensaje", "Ocurrió un error inesperado en el BFF. Por favor, intenta de nuevo.");
        e.printStackTrace(); // Para ver el error en la consola de Docker
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}