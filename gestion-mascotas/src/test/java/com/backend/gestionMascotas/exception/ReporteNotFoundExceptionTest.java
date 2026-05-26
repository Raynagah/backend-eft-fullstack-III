package com.backend.gestionMascotas.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReporteNotFoundExceptionTest {

    @Test
    void cuandoSeLanzaExcepcion_entoncesGeneraMensajeCorrecto() {
        // Arrange
        Long idPrueba = 99L;

        // Act
        ReporteNotFoundException excepcion = new ReporteNotFoundException(idPrueba);

        // Assert
        assertEquals("El reporte con ID 99 no existe.", excepcion.getMessage());
    }
}