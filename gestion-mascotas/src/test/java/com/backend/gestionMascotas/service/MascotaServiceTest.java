package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.client.GeolocalizacionClient;
import com.backend.gestionMascotas.dto.ReporteResponseDTO;
import com.backend.gestionMascotas.exception.ReporteNotFoundException;
import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.repository.MascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    // Agregamos los otros componentes que usa tu Service
    @Mock
    private ReporteFactory reporteFactory;

    @Mock
    private GeolocalizacionClient geoClient;

    @InjectMocks
    private MascotaService mascotaService;

    private ReporteMascota mascotaMock;

    @BeforeEach
    void setUp() {
        mascotaMock = new ReporteMascota();
        mascotaMock.setId(1L);
        mascotaMock.setNombre("Cachupín");
        mascotaMock.setEspecie("Perro");
        mascotaMock.setTipoReporte("PERDIDA");
    }

    // --- TEST 1: Buscar por ID (Happy Path) ---
    @Test
    void cuandoObtenerReportePorIdExiste_entoncesRetornaDTO() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        ReporteResponseDTO resultado = mascotaService.obtenerReportePorId(1L);

        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());
        verify(mascotaRepository, times(1)).findById(1L);
    }

    // --- TEST 2: Buscar por ID (Error Path) ---
    @Test
    void cuandoObtenerReportePorIdNoExiste_entoncesLanzaExcepcion() {
        // Simulamos que la base de datos devuelve vacío
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        // Verificamos que al llamar al servicio, se lance la excepción correcta
        assertThrows(ReporteNotFoundException.class, () -> {
            mascotaService.obtenerReportePorId(99L);
        });

        verify(mascotaRepository, times(1)).findById(99L);
    }

    // --- TEST 3: Obtener Todos ---
    @Test
    void cuandoObtenerTodos_entoncesRetornaListaDeDTOs() {
        when(mascotaRepository.findAll()).thenReturn(List.of(mascotaMock));

        List<ReporteResponseDTO> resultado = mascotaService.obtenerTodosLosReportes();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Cachupín", resultado.get(0).nombre());
        verify(mascotaRepository, times(1)).findAll();
    }

    // --- TEST 4: Obtener por Tipo ---
    @Test
    void cuandoObtenerPorTipo_entoncesRetornaListaFiltrada() {
        when(mascotaRepository.findByTipoReporte("PERDIDA")).thenReturn(List.of(mascotaMock));

        List<ReporteResponseDTO> resultado = mascotaService.obtenerReportesPorTipo("PERDIDA");

        assertFalse(resultado.isEmpty());
        assertEquals("PERDIDA", resultado.get(0).tipoReporte());
        verify(mascotaRepository, times(1)).findByTipoReporte("PERDIDA");
    }

    // --- TEST 5: Eliminar Reporte ---
    @Test
    void cuandoEliminarReporteExiste_entoncesEliminaSinErrores() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        // Act: Ejecutamos el métodoo que no retorna nada (void)
        assertDoesNotThrow(() -> mascotaService.eliminarReporte(1L));

        // Verificamos que se llamó al cliente geo y al borrado del repositorio
        verify(geoClient, times(1)).eliminarUbicacion(1L);
        verify(mascotaRepository, times(1)).delete(mascotaMock);
    }

    @Test
    void cuandoRegistrarReporteExitoso_entoncesRetornaDTO() {
        // ARRANGE: Preparamos el DTO de entrada (Request)
        com.backend.gestionMascotas.dto.ReporteRequestDTO requestDto =
                new com.backend.gestionMascotas.dto.ReporteRequestDTO(
                        125L, "PERDIDA", "Cachupín", "Perro", "Raza", "Dorado",
                        "Grande", "Juan Pérez", "+56912345678", "juan@email.com",
                        "http://foto.jpg", -34.6037, -58.3816
                );

        // Definimos el comportamiento de los Mocks
        when(reporteFactory.crearReporte(requestDto)).thenReturn(mascotaMock);
        when(mascotaRepository.save(any(ReporteMascota.class))).thenReturn(mascotaMock);
        // Nota: geoClient.registrarUbicacion es void, por defecto Mockito no hace nada (lo deja pasar)

        // ACT: Ejecutamos el métodoo real
        ReporteResponseDTO resultado = mascotaService.registrarReporte(requestDto);

        // ASSERT: Verificaciones básicas
        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());

        // Verificamos que se usaron las dependencias correctamente
        verify(reporteFactory, times(1)).crearReporte(requestDto);
        verify(mascotaRepository, times(2)).save(any(ReporteMascota.class)); // Se llama 2 veces en tu service (al inicio y al completar la saga)
        verify(geoClient, times(1)).registrarUbicacion(anyMap());
    }

    // --- TEST: Eliminar Reporte (Error Path - Faltaba la lambda) ---
    @Test
    void cuandoEliminarReporteNoExiste_entoncesLanzaExcepcion() {
        // Arrange: Simulamos que no se encuentra en la base de datos
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: Verificamos que lance la excepción
        assertThrows(ReporteNotFoundException.class, () -> {
            mascotaService.eliminarReporte(99L);
        });

        // Verificamos que NO se llamó al cliente geo ni al delete
        verify(geoClient, never()).eliminarUbicacion(anyLong());
        verify(mascotaRepository, never()).delete(any());
    }

    // --- TEST: Compensar Reporte (Saga - Faltaba métodoo y lambda) ---
    @Test
    void cuandoCompensarReporteExiste_entoncesActualizaEstado() {
        // Arrange: Simulamos que la mascota existe
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));
        // Aquí no necesitamos mockear el save si solo nos importa que se llame

        // Act
        mascotaService.compensarReporte(1L);

        // Assert: Verificamos que se buscó y luego se guardó (con el estado compensado)
        verify(mascotaRepository, times(1)).findById(1L);
        verify(mascotaRepository, times(1)).save(mascotaMock);
    }

    // --- TEST: Registrar Reporte Falla (Saga Catch - Corrección) ---
    @Test
    void cuandoRegistrarReporteFallaGeo_entoncesCompensaYRetornaDto() {
        // Arrange
        com.backend.gestionMascotas.dto.ReporteRequestDTO requestDto =
                new com.backend.gestionMascotas.dto.ReporteRequestDTO(
                        125L, "PERDIDA", "Cachupín", "Perro", "Raza", "Dorado",
                        "Grande", "Juan", "123", "juan@email.com", "url", -34.6, -58.3
                );

        when(reporteFactory.crearReporte(requestDto)).thenReturn(mascotaMock);
        // Al guardar la primera vez, retorna el mock
        when(mascotaRepository.save(any(ReporteMascota.class))).thenReturn(mascotaMock);

        // Simulamos que la API de mapas falla
        doThrow(new RuntimeException("Fallo en la API de Mapas")).when(geoClient).registrarUbicacion(anyMap());

        // ¡IMPORTANTE! Como tu catch llama a compensarReporte() y este hace un findById, necesitamos mockearlo
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        // Act: Ahora no usamos assertThrows, porque tu código captura el error y retorna el DTO
        ReporteResponseDTO resultado = mascotaService.registrarReporte(requestDto);

        // Assert
        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());

        // Verificamos el flujo interno
        verify(geoClient, times(1)).registrarUbicacion(anyMap()); // Intentó registrar
        verify(mascotaRepository, times(1)).findById(1L); // Llamó a buscarlo para compensar
        verify(mascotaRepository, times(2)).save(any(ReporteMascota.class)); // Guardó al inicio y al compensar
    }

    // --- TEST: Eliminar Reporte Falla Geo (Catch de eliminación) ---
    @Test
    void cuandoEliminarReporteFallaGeo_entoncesCapturaErrorYBorraMascota() {
        // Arrange
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));
        // Simulamos que al intentar borrar la ubicación lanza un error
        doThrow(new RuntimeException("Error geo")).when(geoClient).eliminarUbicacion(1L);

        // Act
        assertDoesNotThrow(() -> mascotaService.eliminarReporte(1L));

        // Assert
        verify(geoClient, times(1)).eliminarUbicacion(1L);
        verify(mascotaRepository, times(1)).delete(mascotaMock); // Se asegura de que el borrado local SÍ ocurra
    }
}