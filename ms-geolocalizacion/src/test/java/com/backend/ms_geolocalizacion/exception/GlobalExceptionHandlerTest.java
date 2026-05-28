package com.backend.ms_geolocalizacion.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private ServletWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Simulamos la petición HTTP para que el getRequestURI() no falle
        when(webRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void handleResourceNotFound_DebeRetornar404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertEquals("Recurso no encontrado", response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleBadRequest_DebeRetornar400() {
        BadRequestException ex = new BadRequestException("Petición inválida");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Petición inválida", response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleGlobalException_DebeRetornar500() {
        Exception ex = new Exception("Error de base de datos");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Ocurrió un error inesperado en el servidor: Error de base de datos", response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }
}