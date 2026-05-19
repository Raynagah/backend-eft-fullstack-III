package com.backend.gestionMascotas.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.backend.gestionMascotas.org.springdoc.DummySpringdocException;
import com.backend.gestionMascotas.io.swagger.DummySwaggerException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Instanciamos el handler como una clase normal
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void cuandoOcurreReporteNotFoundException_entoncesRetornaMapConError() {
        // Arrange
        ReporteNotFoundException excepcion = new ReporteNotFoundException(1L);

        // Act
        Map<String, String> resultado = globalExceptionHandler.manejarReporteNoEncontrado(excepcion);

        // Assert
        assertEquals("Reporte no encontrado", resultado.get("error"));
        assertEquals("El reporte con ID 1 no existe.", resultado.get("mensaje"));
    }

    @Test
    void cuandoOcurreRuntimeException_entoncesRetornaMapConMensaje() {
        // Arrange
        RuntimeException excepcion = new RuntimeException("Error en la base de datos");

        // Act
        Map<String, String> resultado = globalExceptionHandler.manejarRuntime(excepcion);

        // Assert
        assertEquals("Error en la base de datos", resultado.get("mensaje"));
    }

    @Test
    void cuandoOcurreExceptionGeneral_entoncesRetornaMapConMensajeInterno() throws Exception {
        // Arrange
        Exception excepcion = new Exception("Fallo de conexión");

        // Act
        Map<String, String> resultado = globalExceptionHandler.manejarGeneral(excepcion);

        // Assert
        assertEquals("Error interno: Fallo de conexión", resultado.get("mensaje"));
    }

    @Test
    void cuandoOcurreErrorDeValidacion_entoncesRetornaMapConCamposInvalidos() {
        // Arrange
        // Simulamos la excepción de validación y su BindingResult
        MethodArgumentNotValidException excepcionMock = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Creamos un error ficticio (ej. el campo 'nombre' está vacío)
        FieldError errorCampo = new FieldError("dto", "nombre", "El nombre es obligatorio");

        // Configuramos los mocks
        when(excepcionMock.getBindingResult()).thenReturn(bindingResultMock);
        when(bindingResultMock.getFieldErrors()).thenReturn(List.of(errorCampo));

        // Act
        Map<String, String> resultado = globalExceptionHandler.manejarValidaciones(excepcionMock);

        // Assert
        assertTrue(resultado.containsKey("nombre"));
        assertEquals("El nombre es obligatorio", resultado.get("nombre"));
    }
    // --- TEST: Rama 1 del if (org.springdoc) ---
    @Test
    void cuandoExcepcionEsDeSpringdoc_entoncesLanzaExcepcionOriginal() {
        // Arrange
        DummySpringdocException excepcion = new DummySpringdocException("Error interno de documentación");

        // Act & Assert
        // Verificamos que el handler no atrape la excepción, sino que la vuelva a lanzar
        assertThrows(DummySpringdocException.class, () -> {
            globalExceptionHandler.manejarGeneral(excepcion);
        });
    }

    // --- TEST: Rama 2 del if (io.swagger) ---
    @Test
    void cuandoExcepcionEsDeSwagger_entoncesLanzaExcepcionOriginal() {
        // Arrange
        DummySwaggerException excepcion = new DummySwaggerException("Error en parser de swagger");

        // Act & Assert
        assertThrows(DummySwaggerException.class, () -> {
            globalExceptionHandler.manejarGeneral(excepcion);
        });
    }
}