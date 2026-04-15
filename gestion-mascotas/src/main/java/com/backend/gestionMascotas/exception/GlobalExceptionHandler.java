package com.backend.gestionMascotas.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de validación (@Valid) en el DTO.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );
        return errores;
    }

    /**
     * Captura específicamente cuando un reporte no existe (404).
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReporteNotFoundException.class)
    public Map<String, String> manejarReporteNoEncontrado(ReporteNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Reporte no encontrado");
        error.put("mensaje", ex.getMessage());
        return error;
    }

    /**
     * Captura errores genéricos de ejecución (400).
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> manejarRuntime(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return error;
    }

    /**
     * Captura cualquier otro error no previsto (500).
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> manejarGeneral(Exception ex) throws Exception {
        // Si la petición es para la documentación, NO captures el error
        // Deja que SpringDoc lo maneje para ver el detalle
        if (ex.getClass().getName().contains("org.springdoc") || 
            ex.getClass().getName().contains("io.swagger")) {
            throw ex; 
        }

        ex.printStackTrace(); // Esto imprimirá el error real en tu consola

        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error interno: " + ex.getMessage()); // Agregamos ex.getMessage() para debug
        return error;
    }
}