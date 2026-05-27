package com.backend.ms_geolocalizacion.exception;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class ExceptionsModelTest {

    @Test
    void testErrorResponseLombokMethods() {
        // Probamos el Builder y los Setters/Getters autogenerados por Lombok
        LocalDateTime now = LocalDateTime.now();
        
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(now)
                .status(403)
                .error("Forbidden")
                .message("Acceso denegado")
                .path("/api/seguridad")
                .build();

        assertEquals(now, response.getTimestamp());
        assertEquals(403, response.getStatus());
        assertEquals("Forbidden", response.getError());
        assertEquals("Acceso denegado", response.getMessage());
        assertEquals("/api/seguridad", response.getPath());

        // Probar setters
        response.setStatus(401);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testBadRequestExceptionConstructors() {
        BadRequestException exception = new BadRequestException("Mensaje de error");
        assertNotNull(exception);
        assertEquals("Mensaje de error", exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionConstructors() {
        ResourceNotFoundException exception = new ResourceNotFoundException("No encontrado");
        assertNotNull(exception);
        assertEquals("No encontrado", exception.getMessage());
    }
}