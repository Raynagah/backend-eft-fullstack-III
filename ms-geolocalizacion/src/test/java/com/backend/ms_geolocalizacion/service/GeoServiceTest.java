package com.backend.ms_geolocalizacion.service;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoServiceTest {

    @Mock
    private UbicacionAlertaRepository repository;

    @InjectMocks
    private GeoService geoService;

    private UbicacionAlerta ubicacion;

    @BeforeEach
    void setUp() {
        ubicacion = new UbicacionAlerta();
        ubicacion.setId(1L);
        ubicacion.setReporteId(100L);
        ubicacion.setLatitud(-33.4489); // Coordenadas de ejemplo
        ubicacion.setLongitud(-70.6693);
    }

    @Test
    void registrarUbicacion_DebeGuardarYRetornarObjeto() {
        when(repository.save(any(UbicacionAlerta.class))).thenReturn(ubicacion);

        UbicacionAlerta resultado = geoService.registrarUbicacion(ubicacion);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repository, times(1)).save(ubicacion);
    }

    @Test
    void obtenerTodas_DebeRetornarListaDeUbicaciones() {
        when(repository.findAll()).thenReturn(List.of(ubicacion));

        List<UbicacionAlerta> resultados = geoService.obtenerTodas();

        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
    }

    @Test
    void buscarCercanas_DebeFiltrarPorRadioCorrectamente() {
        // PREPARACIÓN: Creamos dos puntos. Uno muy cerca y otro muy lejos.
        UbicacionAlerta cercana = new UbicacionAlerta();
        cercana.setLatitud(0.01); // Muy cerca del punto 0.0
        cercana.setLongitud(0.01);

        UbicacionAlerta lejana = new UbicacionAlerta();
        lejana.setLatitud(5.0); // A cientos de kilómetros del punto 0.0
        lejana.setLongitud(5.0);

        when(repository.findAll()).thenReturn(List.of(cercana, lejana));

        // ACCIÓN: Buscamos en el punto 0.0 con un radio de 50 kilómetros
        List<UbicacionAlerta> resultados = geoService.buscarCercanas(0.0, 0.0, 50.0);

        // VERIFICACIÓN: La fórmula de Haversine debe incluir a la 'cercana' y descartar a la 'lejana'
        assertEquals(1, resultados.size());
        assertEquals(0.01, resultados.get(0).getLatitud());
    }

    @Test
    void eliminarUbicacion_CuandoExiste_DebeRetornarTrue() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        boolean resultado = geoService.eliminarUbicacion(1L);

        assertTrue(resultado);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarUbicacion_CuandoNoExiste_DebeRetornarFalse() {
        when(repository.existsById(2L)).thenReturn(false);

        boolean resultado = geoService.eliminarUbicacion(2L);

        assertFalse(resultado);
        verify(repository, never()).deleteById(anyLong()); // Nunca debe llamar al delete
    }

    @Test
    void obtenerPorReporteId_CuandoExiste_DebeRetornarObjeto() {
        when(repository.findByReporteId(100L)).thenReturn(Optional.of(ubicacion));

        UbicacionAlerta resultado = geoService.obtenerPorReporteId(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getReporteId());
    }

    @Test
    void obtenerPorReporteId_CuandoNoExiste_DebeRetornarNull() {
        when(repository.findByReporteId(999L)).thenReturn(Optional.empty());

        UbicacionAlerta resultado = geoService.obtenerPorReporteId(999L);

        assertNull(resultado);
    }
}