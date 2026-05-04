package com.backend.bff.config;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Manejo de caídas de otros microservicios (mediante Feign x.x)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeignException(FeignException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Servicio temporalmente no disponible");
        error.put("mensaje", "Estamos teniendo problemas para procesar la información en nuestros servidores internos :(");
        System.err.println("Error de Feign: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // 2. Manejo de errores de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> erroresDetallados = new HashMap<>();

        // Extraemos cada error de los campos
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            erroresDetallados.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> respuestaFinal = new HashMap<>();
        respuestaFinal.put("error", "Datos de formulario inválidos");
        respuestaFinal.put("detalles", erroresDetallados);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaFinal);
    }

    // 3. Manejo de cualquier otro error inesperado (Catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor");
        error.put("mensaje", "Ocurrió un error inesperado en el BFF. Por favor, intenta de nuevo.");
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}