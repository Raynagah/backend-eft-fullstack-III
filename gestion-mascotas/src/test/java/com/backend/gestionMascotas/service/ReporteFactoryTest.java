package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReporteFactoryTest {

    private ReporteFactory reporteFactory;

    @BeforeEach
    void setUp() {
        // Al no tener dependencias inyectadas, lo instanciamos normalmente
        reporteFactory = new ReporteFactory();
    }

    // --- Camino 1: Tipo PERDIDA ---
    @Test
    void cuandoCrearReporteEsPerdida_entoncesCreaMascotaExitosamente() {
        // Arrange
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "PERDIDA", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        // Act
        ReporteMascota resultado = reporteFactory.crearReporte(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals("PERDIDA", resultado.getTipoReporte());
        assertEquals("Kuky", resultado.getNombre());
        assertEquals(125L, resultado.getUsuarioId());
    }

    // --- Camino 2: Tipo ENCONTRADA (y probamos que soporte minúsculas por el .toUpperCase()) ---
    @Test
    void cuandoCrearReporteEsEncontrada_entoncesCreaMascotaExitosamente() {
        // Arrange (enviamos "encontrada" en minúsculas para probar esa línea de tu código)
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "encontrada", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        // Act
        ReporteMascota resultado = reporteFactory.crearReporte(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals("ENCONTRADA", resultado.getTipoReporte());
    }

    // --- Camino 3: Excepción (Else) ---
    @Test
    void cuandoCrearReporteEsInvalido_entoncesLanzaExcepcion() {
        // Arrange (Un tipo de reporte que no existe)
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "ROBADA", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            reporteFactory.crearReporte(dto);
        });

        // Verificamos que lance el mensaje exacto que configuraste en tu Factory
        assertEquals("El tipo de reporte debe ser 'PERDIDA' o 'ENCONTRADA'", excepcion.getMessage());
    }
}