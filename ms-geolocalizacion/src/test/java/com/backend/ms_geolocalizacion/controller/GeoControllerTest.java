package com.backend.ms_geolocalizacion.controller;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.service.GeoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        // Simulamos que el servicio devuelve true (sí lo eliminó)
        when(geoService.eliminarUbicacion(1L)).thenReturn(true);

        ResponseEntity<Void> response = geoController.eliminar(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void eliminar_CuandoNoExiste_DebeRetornar404NotFound() {
        // Simulamos que el servicio devuelve false (no lo encontró)
        when(geoService.eliminarUbicacion(2L)).thenReturn(false);

        ResponseEntity<Void> response = geoController.eliminar(2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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