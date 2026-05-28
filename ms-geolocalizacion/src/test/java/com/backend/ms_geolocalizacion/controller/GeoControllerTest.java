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

@ExtendWith(MockitoExtension.class)
class GeoControllerTest {

    @Mock
    private GeoService geoService;

    @InjectMocks
    private GeoController geoController;

    private UbicacionAlerta ubicacion;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ubicacion = new UbicacionAlerta();
        ubicacion.setId(1L);
        ubicacion.setReporteId(100L);
    }

    @Test
    void registrar_DebeRetornar201Created() {
        when(geoService.registrarUbicacion(any(UbicacionAlerta.class))).thenReturn(ubicacion);

        ResponseEntity<UbicacionAlerta> response = geoController.registrar(ubicacion);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void obtenerTodas_DebeRetornar200OkYLista() {
        when(geoService.obtenerTodas()).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionAlerta>> response = geoController.obtenerTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void buscarCercanas_DebeRetornar200Ok() {
        when(geoService.buscarCercanas(10.0, 10.0, 50.0)).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionAlerta>> response = geoController.buscarCercanas(10.0, 10.0, 50.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void eliminar_CuandoExiste_DebeRetornar204NoContent() {
        // Al retornar void, usamos doNothing()
        doNothing().when(geoService).eliminarUbicacion(1L);

        ResponseEntity<Void> response = geoController.eliminar(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(geoService, times(1)).eliminarUbicacion(1L);
    }

    @Test
    void eliminar_CuandoNoExiste_DebePropagarResourceNotFoundException() {
        // 1. Simulamos que el servicio lanza la excepción por no encontrar el ID
        doThrow(new ResourceNotFoundException("No se encontró la ubicación"))
                .when(geoService).eliminarUbicacion(2L);

        // 2. Asignarlo a una variable 'exception'
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoController.eliminar(2L)
        );

        // 3. Validamos que el mensaje dentro de la excepción sea el esperado
        assertEquals("No se encontró la ubicación", exception.getMessage());
    }

    @Test
    void obtenerPorReporte_DebeRetornar200OkYObjeto() {
        when(geoService.obtenerPorReporteId(100L)).thenReturn(ubicacion);

        ResponseEntity<UbicacionAlerta> response = geoController.obtenerPorReporte(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getReporteId());
    }
}