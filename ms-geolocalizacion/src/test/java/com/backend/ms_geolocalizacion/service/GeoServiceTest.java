package com.backend.ms_geolocalizacion.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.ms_geolocalizacion.exception.BadRequestException;
import com.backend.ms_geolocalizacion.exception.ResourceNotFoundException;
import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;

@ExtendWith(MockitoExtension.class)
class GeoServiceTest {

    @Mock
    private UbicacionAlertaRepository repository;

    @InjectMocks
    private GeoService geoService;

    private UbicacionAlerta ubicacion;

    @BeforeEach
    @SuppressWarnings("unused") // Suprime el warning de "setUp is never used"
    void setUp() {
        ubicacion = new UbicacionAlerta();
        ubicacion.setId(1L);
        ubicacion.setReporteId(100L);
        ubicacion.setLatitud(-33.4489); // Santiago, Chile
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
    void registrarUbicacion_CuandoReporteIdEsNull_DebeLanzarBadRequestException() {
        ubicacion.setReporteId(null);

        // Se guarda en una variable para quitar el warning y se valida el mensaje
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertEquals("El ID de reporte es obligatorio.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUbicacion_CuandoLatitudInvalida_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(95.0); // Fuera del rango de -90 a 90

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertEquals("La latitud debe ser un valor numérico válido entre -90.0 y 90.0.", exception.getMessage());
        verify(repository, never()).save(any());
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
        UbicacionAlerta cercana = new UbicacionAlerta();
        cercana.setLatitud(0.01); 
        cercana.setLongitud(0.01);

        UbicacionAlerta lejana = new UbicacionAlerta();
        lejana.setLatitud(5.0); 
        lejana.setLongitud(5.0);

        when(repository.findAll()).thenReturn(List.of(cercana, lejana));

        List<UbicacionAlerta> resultados = geoService.buscarCercanas(0.0, 0.0, 50.0);

        assertEquals(1, resultados.size());
        assertEquals(0.01, resultados.get(0).getLatitud());
    }

    @Test
    void buscarCercanas_CuandoRadioEsInvalido_DebeLanzarBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(0.0, 0.0, -10.0)
        );
        
        assertEquals("El radio de búsqueda debe ser mayor a 0 KM.", exception.getMessage());
    }

    @Test
    void eliminarUbicacion_CuandoExiste_DebeEliminarSinErrores() {
        when(repository.findById(1L)).thenReturn(Optional.of(ubicacion));
        doNothing().when(repository).delete(ubicacion);

        assertDoesNotThrow(() -> geoService.eliminarUbicacion(1L));
        verify(repository, times(1)).delete(ubicacion);
    }

    @Test
    void eliminarUbicacion_CuandoNoExiste_DebeLanzarResourceNotFoundException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoService.eliminarUbicacion(2L)
        );
        
        assertEquals("No se encontró ninguna ubicación con el ID: 2", exception.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    void obtenerPorReporteId_CuandoExiste_DebeRetornarObjeto() {
        when(repository.findByReporteId(100L)).thenReturn(Optional.of(ubicacion));

        UbicacionAlerta resultado = geoService.obtenerPorReporteId(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getReporteId());
    }

    @Test
    void obtenerPorReporteId_CuandoNoExiste_DebeLanzarResourceNotFoundException() {
        when(repository.findByReporteId(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoService.obtenerPorReporteId(999L)
        );
        
        assertEquals("No se encontró ninguna ubicación asociada al reporte con ID: 999", exception.getMessage());
    }

    @Test
    void registrarUbicacion_CuandoLatitudEsNull_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(null);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception); // Evita el warning del IDE
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUbicacion_CuandoLatitudMenorAMenos90_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(-91.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUbicacion_CuandoLongitudEsNull_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(null);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUbicacion_CuandoLongitudMenorAMenos180_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(-181.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    @Test
    void registrarUbicacion_CuandoLongitudMayorA180_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(181.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    @Test
    void buscarCercanas_CuandoRadioEsCero_DebeLanzarBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(-33.0, -70.0, 0.0)
        );
        
        assertNotNull(exception);
    }

    @Test
    void buscarCercanas_CuandoLatitudOlongitudInvalida_DebeLanzarBadRequestException() {
        BadRequestException exceptionLat = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(null, -70.0, 50.0)
        );
        
        BadRequestException exceptionLon = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(-33.0, null, 50.0)
        );
        
        assertNotNull(exceptionLat);
        assertNotNull(exceptionLon);
    }
}