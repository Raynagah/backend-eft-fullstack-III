package com.backend.notificaciones.service;

import com.backend.notificaciones.client.ReporteClient;
import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.dto.ReporteRequestDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    // Simulo mi repositorio para no usar base de datos real
    @Mock
    private NotificacionRepository notificacionRepository;

    // Simulo mi cliente Feign para no hacer llamadas HTTP reales al microservicio de reportes
    @Mock
    private ReporteClient reporteClient;

    // Inyecto mis mocks en el servicio
    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        // Datos base confiables para usar en varios tests
        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setReporteId(100L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setLeido(false);
    }

    @Test
    void procesarNotificaciones_ConSimilitudAlta_DebeGuardarYNotificar() {
        // PREPARACIÓN: Creo un Match que supera el umbral (>= 85%)
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(88.5);
        matchDTO.setMensaje("Encontramos algo.");

        // Simulo la respuesta de mi otro microservicio vía Feign
        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // ACCIÓN: Ejecuto el método con mi Match
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Capturo el objeto exacto que mi código intentó guardar en BD
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();
        assertEquals(5L, guardada.getUsuarioId()); // Valido que haya mapeado los datos de Feign correctamente
        assertEquals(88.5, guardada.getPorcentajeSimilitud());
        assertEquals("user@test.com", guardada.getEmailUsuario());
    }

    @Test
    void procesarNotificaciones_ConSimilitudAltaYTitulo_DebeUsarTituloProporcionado() {
        // PREPARACIÓN: Creo un Match que supera el umbral (>= 85%)
        // Y esta vez SÍ le paso un título para cubrir la otra rama del operador ternario
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(95.0);
        matchDTO.setTitulo("¡Encontramos a Firulais!"); // <-- ¡El dato clave para el 100% branch coverage!
        matchDTO.setMensaje("Tu mascota está a salvo.");

        // Simulo la respuesta del microservicio vía Feign
        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // ACCIÓN: Ejecuto el procesamiento
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Capturo lo que se intentó guardar
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();

        // Compruebo que haya usado MI título y no el valor por defecto
        assertEquals("¡Encontramos a Firulais!", guardada.getTitulo());
    }

    @Test
    void procesarNotificaciones_ConSimilitudBaja_NoDebeGuardar() {
        // PREPARACIÓN: Creo un Match debajo del umbral (< 85%)
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(50.0);

        // ACCIÓN: Lo proceso
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Compruebo que JAMÁS llamó al microservicio de reportes ni intentó guardar nada
        verify(reporteClient, never()).obtenerReportePorId(anyLong());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void obtenerPorUsuario_DebeRetornarLista() {
        // PREPARACIÓN
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(5L))
                .thenReturn(List.of(notificacionMock));

        // ACCIÓN
        List<Notificacion> resultado = notificacionService.obtenerPorUsuario(5L);

        // VERIFICACIÓN
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void eliminarNotificacion_Existente_DebeBorrar() {
        // PREPARACIÓN: Simulo que el ID existe en la BD
        when(notificacionRepository.existsById(1L)).thenReturn(true);

        // ACCIÓN
        notificacionService.eliminarNotificacion(1L);

        // VERIFICACIÓN: Confirmo que se ejecutó el deleteById
        verify(notificacionRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarNotificacion_NoExistente_DebeLanzarExcepcion() {
        // PREPARACIÓN: Simulo que el ID NO existe
        when(notificacionRepository.existsById(99L)).thenReturn(false);

        // ACCIÓN Y VERIFICACIÓN: Compruebo que la lógica corta el flujo lanzando mi excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.eliminarNotificacion(99L)
        );
        assertEquals("La notificación con ID 99 no existe.", ex.getMessage());
    }

    @Test
    void obtenerTodas_DebeRetornarListaCompleta() {
        // PREPARACIÓN
        when(notificacionRepository.findAll()).thenReturn(List.of(notificacionMock));

        // ACCIÓN
        List<Notificacion> resultado = notificacionService.obtenerTodas();

        // VERIFICACIÓN
        assertEquals(1, resultado.size());
    }

    @Test
    void marcarComoLeida_Exito() {
        // PREPARACIÓN: Simulo que encuentra la notificación y que inicialmente está como NO leída
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionMock));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionMock);

        // ACCIÓN
        Notificacion resultado = notificacionService.marcarComoLeida(1L);

        // VERIFICACIÓN: Compruebo que la lógica cambió el booleano a true
        assertTrue(resultado.isLeido());
        verify(notificacionRepository).save(notificacionMock);
    }

    @Test
    void marcarComoLeida_NoExistente_DebeLanzarExcepcion() {
        // PREPARACIÓN
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.marcarComoLeida(99L)
        );
        assertEquals("Notificación no encontrada con ID: 99", ex.getMessage());
    }
}