package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Función: ReporteFactoryTest (Clase de Pruebas)
 * Título: Pruebas Unitarias de la Fábrica de Reportes
 * Descripción: Verifica la correcta instanciación de la entidad ReporteMascota a través 
 * del patrón Factory, validando el mapeo de atributos y las reglas de negocio estrictas 
 * relacionadas con los tipos de reporte permitidos.
 */
public class ReporteFactoryTest {

    private ReporteFactory reporteFactory;

    @BeforeEach
    void setUp() {
        reporteFactory = new ReporteFactory();
    }

    /**
     * Función: cuandoCrearReporteEsPerdida_entoncesCreaMascotaExitosamente
     * Título: Validar creación de reporte (Tipo PERDIDA)
     * Descripción: Comprueba que la fábrica instancie y retorne correctamente la entidad 
     * cuando los datos de entrada contienen el tipo de reporte válido "PERDIDA".
     */
    @Test
    void cuandoCrearReporteEsPerdida_entoncesCreaMascotaExitosamente() {
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "PERDIDA", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        ReporteMascota resultado = reporteFactory.crearReporte(dto);

        assertNotNull(resultado);
        assertEquals("PERDIDA", resultado.getTipoReporte());
        assertEquals("Kuky", resultado.getNombre());
        assertEquals(125L, resultado.getUsuarioId());
    }

    /**
     * Función: cuandoCrearReporteEsEncontrada_entoncesCreaMascotaExitosamente
     * Título: Validar creación de reporte (Tipo ENCONTRADA) y formato
     * Descripción: Asegura que la fábrica procese de forma segura entradas en minúsculas 
     * y construya la entidad estandarizando el estado a mayúsculas ("ENCONTRADA").
     */
    @Test
    void cuandoCrearReporteEsEncontrada_entoncesCreaMascotaExitosamente() {
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "encontrada", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        ReporteMascota resultado = reporteFactory.crearReporte(dto);

        assertNotNull(resultado);
        assertEquals("ENCONTRADA", resultado.getTipoReporte());
    }

    /**
     * Función: cuandoCrearReporteEsInvalido_entoncesLanzaExcepcion
     * Título: Validar rechazo por tipo de reporte inválido
     * Descripción: Verifica que el sistema arroje una IllegalArgumentException con el mensaje 
     * descriptivo correcto si el tipo de reporte proporcionado no pertenece a los valores autorizados.
     */
    @Test
    void cuandoCrearReporteEsInvalido_entoncesLanzaExcepcion() {
        ReporteRequestDTO dto = new ReporteRequestDTO(
                125L, "ROBADA", "Kuky", "Perro", "Mestizo", "Blanco",
                "Pequeño", "Juan", "+569123", "juan@correo.com", "url", -34.0, -58.0
        );

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            reporteFactory.crearReporte(dto);
        });

        assertEquals("El tipo de reporte debe ser 'PERDIDA' o 'ENCONTRADA'", excepcion.getMessage());
    }
}