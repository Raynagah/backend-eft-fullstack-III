package com.backend.ms_geolocalizacion.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.backend.ms_geolocalizacion.exception.ResourceNotFoundException;
import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.service.GeoService;

/**
 * Función: GeoControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Geolocalización
 * Descripción: Verifica el comportamiento de los endpoints REST encargados de la gestión de 
 * ubicaciones, asegurando que las respuestas HTTP, el manejo de excepciones y la 
 * interacción con el servicio subyacente operen correctamente.
 */
@ExtendWith(MockitoExtension.class)
class GeoControllerTest {

    @Mock
    private GeoService geoService;

    @InjectMocks
    private GeoController geoController;

    private UbicacionAlerta ubicacion;

    @BeforeEach
    void setUp() {
        ubicacion = new UbicacionAlerta();
        ubicacion.setId(1L);
        ubicacion.setReporteId(100L);
    }

    /**
     * Función: registrar_DebeRetornar201Created
     * Título: Validar registro exitoso de ubicación
     * Descripción: Comprueba que al enviar una ubicación válida, el controlador llame al servicio 
     * correspondiente y retorne la entidad persistida junto con un código HTTP 201 (CREATED).
     */
    @Test
    void registrar_DebeRetornar201Created() {
        when(geoService.registrarUbicacion(any(UbicacionAlerta.class))).thenReturn(ubicacion);

        ResponseEntity<UbicacionAlerta> response = geoController.registrar(ubicacion);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    /**
     * Función: obtenerTodas_DebeRetornar200OkYLista
     * Título: Validar obtención general de ubicaciones
     * Descripción: Asegura que el endpoint de consulta general retorne una lista poblada 
     * de ubicaciones acompañada de un código HTTP 200 (OK).
     */
    @Test
    void obtenerTodas_DebeRetornar200OkYLista() {
        when(geoService.obtenerTodas()).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionAlerta>> response = geoController.obtenerTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    /**
     * Función: buscarCercanas_DebeRetornar200Ok
     * Título: Validar búsqueda espacial por proximidad
     * Descripción: Verifica que la consulta por latitud, longitud y radio delegue correctamente 
     * los parámetros al servicio y devuelva los resultados con un código HTTP 200 (OK).
     */
    @Test
    void buscarCercanas_DebeRetornar200Ok() {
        when(geoService.buscarCercanas(10.0, 10.0, 50.0)).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionAlerta>> response = geoController.buscarCercanas(10.0, 10.0, 50.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    /**
     * Función: eliminar_CuandoExiste_DebeRetornar204NoContent
     * Título: Validar eliminación exitosa
     * Descripción: Comprueba que al solicitar la eliminación de un ID existente, el controlador 
     * responda correctamente con un código HTTP 204 (NO CONTENT).
     */
    @Test
    void eliminar_CuandoExiste_DebeRetornar204NoContent() {
        doNothing().when(geoService).eliminarUbicacion(1L);

        ResponseEntity<Void> response = geoController.eliminar(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(geoService, times(1)).eliminarUbicacion(1L);
    }

    /**
     * Función: eliminar_CuandoNoExiste_DebePropagarResourceNotFoundException
     * Título: Validar propagación de error al eliminar
     * Descripción: Simula un fallo en el servicio (ID inexistente) y asegura que el controlador 
     * no enmascare la excepción, permitiendo que el GlobalExceptionHandler la capture.
     */
    @Test
    void eliminar_CuandoNoExiste_DebePropagarResourceNotFoundException() {
        doThrow(new ResourceNotFoundException("No se encontró la ubicación"))
                .when(geoService).eliminarUbicacion(2L);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoController.eliminar(2L)
        );

        assertEquals("No se encontró la ubicación", exception.getMessage());
    }

    /**
     * Función: obtenerPorReporte_DebeRetornar200OkYObjeto
     * Título: Validar búsqueda de ubicación por ID de reporte
     * Descripción: Verifica que el endpoint retorne exitosamente los datos espaciales 
     * asociados a un reporte específico, devolviendo un código HTTP 200 (OK).
     */
    @Test
    void obtenerPorReporte_DebeRetornar200OkYObjeto() {
        when(geoService.obtenerPorReporteId(100L)).thenReturn(ubicacion);

        ResponseEntity<UbicacionAlerta> response = geoController.obtenerPorReporte(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getReporteId());
    }
}