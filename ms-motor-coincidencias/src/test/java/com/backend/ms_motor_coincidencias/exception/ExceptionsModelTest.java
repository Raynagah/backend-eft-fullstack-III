package com.backend.ms_motor_coincidencias.exception;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class ExceptionsModelTest {

    @Test
    void testErrorResponseLombokMethods() {
        // Probamos el Builder y los Setters/Getters autogenerados por Lombok para alcanzar el 100% de cobertura
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

        // Probar un setter explícito
        response.setStatus(401);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testBadRequestExceptionConstructors() {
        BadRequestException exception = new BadRequestException("Mensaje de error bad request");
        assertNotNull(exception);
        assertEquals("Mensaje de error bad request", exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionConstructors() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Mensaje de error not found");
        assertNotNull(exception);
        assertEquals("Mensaje de error not found", exception.getMessage());
    }
}