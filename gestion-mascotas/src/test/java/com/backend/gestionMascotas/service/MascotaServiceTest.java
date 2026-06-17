package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.client.GeolocalizacionClient;
import com.backend.gestionMascotas.dto.ReporteRequestDTO;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Función: MascotaServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Gestión de Mascotas
 * Descripción: Valida la lógica central del dominio, incluyendo el orquestado de 
 * transacciones distribuidas (Saga), compensaciones automáticas, emisión de eventos 
 * asíncronos y operaciones CRUD estándar con sus respectivas validaciones.
 */
@ExtendWith(MockitoExtension.class)
public class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private ReporteFactory reporteFactory;

    @Mock
    private GeolocalizacionClient geoClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

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

    /**
     * Función: cuandoObtenerReportePorIdExiste_entoncesRetornaDTO
     * Título: Obtener reporte exitoso por ID
     * Descripción: Verifica que la consulta por identificador delegue al repositorio y mapee 
     * correctamente la entidad encontrada a su DTO correspondiente.
     */
    @Test
    void cuandoObtenerReportePorIdExiste_entoncesRetornaDTO() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        ReporteResponseDTO resultado = mascotaService.obtenerReportePorId(1L);

        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());
        verify(mascotaRepository, times(1)).findById(1L);
    }

    /**
     * Función: cuandoObtenerReportePorIdNoExiste_entoncesLanzaExcepcion
     * Título: Fallo controlado al buscar ID inexistente
     * Descripción: Asegura que el servicio propague una ReporteNotFoundException si 
     * el identificador provisto no se encuentra en la base de datos.
     */
    @Test
    void cuandoObtenerReportePorIdNoExiste_entoncesLanzaExcepcion() {
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> mascotaService.obtenerReportePorId(99L));
        verify(mascotaRepository, times(1)).findById(99L);
    }

    /**
     * Función: cuandoObtenerTodos_entoncesRetornaListaDeDTOs
     * Título: Obtener listado global de reportes
     * Descripción: Valida que el servicio recupere satisfactoriamente la colección completa 
     * de registros y la transforme en una lista estructurada de objetos DTO.
     */
    @Test
    void cuandoObtenerTodos_entoncesRetornaListaDeDTOs() {
        when(mascotaRepository.findAll()).thenReturn(List.of(mascotaMock));

        List<ReporteResponseDTO> resultado = mascotaService.obtenerTodosLosReportes();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Cachupín", resultado.get(0).nombre());
        verify(mascotaRepository, times(1)).findAll();
    }

    /**
     * Función: cuandoObtenerPorTipo_entoncesRetornaListaFiltrada
     * Título: Obtener reportes categorizados
     * Descripción: Comprueba que el servicio delegue correctamente el filtrado por tipo 
     * (ej. "PERDIDA") al repositorio y retorne los DTOs que cumplen con el criterio.
     */
    @Test
    void cuandoObtenerPorTipo_entoncesRetornaListaFiltrada() {
        when(mascotaRepository.findByTipoReporte("PERDIDA")).thenReturn(List.of(mascotaMock));

        List<ReporteResponseDTO> resultado = mascotaService.obtenerReportesPorTipo("PERDIDA");

        assertFalse(resultado.isEmpty());
        assertEquals("PERDIDA", resultado.get(0).tipoReporte());
        verify(mascotaRepository, times(1)).findByTipoReporte("PERDIDA");
    }

    /**
     * Función: cuandoEliminarReporteExiste_entoncesEliminaSinErrores
     * Título: Flujo exitoso de eliminación integral
     * Descripción: Verifica que al solicitar el borrado de un reporte, se ejecute la eliminación 
     * en el microservicio de geolocalización y, seguidamente, el borrado físico en el repositorio local.
     */
    @Test
    void cuandoEliminarReporteExiste_entoncesEliminaSinErrores() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        assertDoesNotThrow(() -> mascotaService.eliminarReporte(1L));

        verify(geoClient, times(1)).eliminarUbicacion(1L);
        verify(mascotaRepository, times(1)).delete(mascotaMock);
    }

    /**
     * Función: cuandoRegistrarReporteExitoso_entoncesRetornaDTO
     * Título: Flujo exitoso de registro (Orquestación Saga)
     * Descripción: Valida la creación del reporte, su persistencia local, la sincronización 
     * en el cliente geográfico, el envío del evento de mensajería (RabbitMQ) y la confirmación final de la saga.
     */
    @Test
    void cuandoRegistrarReporteExitoso_entoncesRetornaDTO() {
        ReporteRequestDTO requestDto = new ReporteRequestDTO(
                125L, "PERDIDA", "Cachupín", "Perro", "Raza", "Dorado",
                "Grande", "Juan Pérez", "+56912345678", "juan@email.com",
                "http://foto.jpg", -34.6037, -58.3816
        );

        when(reporteFactory.crearReporte(requestDto)).thenReturn(mascotaMock);
        when(mascotaRepository.save(any(ReporteMascota.class))).thenReturn(mascotaMock);

        ReporteResponseDTO resultado = mascotaService.registrarReporte(requestDto);

        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());

        verify(reporteFactory, times(1)).crearReporte(requestDto);
        verify(mascotaRepository, times(2)).save(any(ReporteMascota.class));
        verify(geoClient, times(1)).registrarUbicacion(anyMap());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    /**
     * Función: cuandoEliminarReporteNoExiste_entoncesLanzaExcepcion
     * Título: Abortar eliminación ante reporte inexistente
     * Descripción: Comprueba que el flujo de eliminación detenga su ejecución de inmediato y arroje 
     * una excepción sin invocar recursos externos o locales de borrado.
     */
    @Test
    void cuandoEliminarReporteNoExiste_entoncesLanzaExcepcion() {
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReporteNotFoundException.class, () -> mascotaService.eliminarReporte(99L));

        verify(geoClient, never()).eliminarUbicacion(anyLong());
        verify(mascotaRepository, never()).delete(any());
    }

    /**
     * Función: cuandoCompensarReporteExiste_entoncesActualizaEstado
     * Título: Ejecución manual de compensación Saga
     * Descripción: Verifica que la orden de compensar actualice el estado interno del reporte 
     * indicando una asincronía fallida ("FAILED_SYNC").
     */
    @Test
    void cuandoCompensarReporteExiste_entoncesActualizaEstado() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        mascotaService.compensarReporte(1L);

        verify(mascotaRepository, times(1)).findById(1L);
        verify(mascotaRepository, times(1)).save(mascotaMock);
    }

    /**
     * Función: cuandoRegistrarReporteFallaGeo_entoncesCompensaYRetornaDto
     * Título: Flujo compensatorio automático en registro (Saga Catch)
     * Descripción: Simula un error crítico al invocar el cliente de geolocalización, asegurando 
     * que el sistema intercepte la falla, proceda a compensar el estado localmente, pero no interrumpa la respuesta.
     */
    @Test
    void cuandoRegistrarReporteFallaGeo_entoncesCompensaYRetornaDto() {
        ReporteRequestDTO requestDto = new ReporteRequestDTO(
                125L, "PERDIDA", "Cachupín", "Perro", "Raza", "Dorado",
                "Grande", "Juan", "123", "juan@email.com", "url", -34.6, -58.3
        );

        when(reporteFactory.crearReporte(requestDto)).thenReturn(mascotaMock);
        when(mascotaRepository.save(any(ReporteMascota.class))).thenReturn(mascotaMock);
        doThrow(new RuntimeException("Fallo en la API de Mapas")).when(geoClient).registrarUbicacion(anyMap());
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));

        ReporteResponseDTO resultado = mascotaService.registrarReporte(requestDto);

        assertNotNull(resultado);
        assertEquals("Cachupín", resultado.nombre());

        verify(geoClient, times(1)).registrarUbicacion(anyMap());
        verify(mascotaRepository, times(1)).findById(1L); 
        verify(mascotaRepository, times(2)).save(any(ReporteMascota.class)); 
    }

    /**
     * Función: cuandoEliminarReporteFallaGeo_entoncesCapturaErrorYBorraMascota
     * Título: Tolerancia a fallos externos durante eliminación
     * Descripción: Garantiza que un fallo originado al intentar purgar la ubicación remota no evite 
     * la eliminación física del registro local (Degradación elegante).
     */
    @Test
    void cuandoEliminarReporteFallaGeo_entoncesCapturaErrorYBorraMascota() {
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascotaMock));
        doThrow(new RuntimeException("Error geo")).when(geoClient).eliminarUbicacion(1L);

        assertDoesNotThrow(() -> mascotaService.eliminarReporte(1L));

        verify(geoClient, times(1)).eliminarUbicacion(1L);
        verify(mascotaRepository, times(1)).delete(mascotaMock); 
    }
}