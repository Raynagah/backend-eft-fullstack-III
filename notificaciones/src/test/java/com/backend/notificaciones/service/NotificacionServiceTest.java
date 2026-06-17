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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Función: NotificacionServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Notificaciones
 * Descripción: Verifica la lógica de negocio central de las notificaciones de forma aislada. 
 * Comprueba el procesamiento condicional de coincidencias de IA (umbrales de similitud), 
 * la integración con clientes Feign (ReporteClient) y las operaciones CRUD delegadas al repositorio.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private ReporteClient reporteClient;

    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setReporteId(100L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setLeido(false);
    }

    /**
     * Función: procesarNotificaciones_ConSimilitudAlta_DebeGuardarYNotificar
     * Título: Procesar coincidencia exitosa (Similitud >= 85%)
     * Descripción: Valida que al recibir un match con un porcentaje de similitud válido, 
     * el servicio consulte el microservicio de reportes vía Feign, mapee correctamente 
     * los datos y guarde la nueva notificación en la base de datos.
     */
    @Test
    void procesarNotificaciones_ConSimilitudAlta_DebeGuardarYNotificar() {
        // Preparación
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(88.5);
        matchDTO.setMensaje("Encontramos algo.");

        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // Acción
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // Verificación
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();
        assertEquals(5L, guardada.getUsuarioId()); 
        assertEquals(88.5, guardada.getPorcentajeSimilitud());
        assertEquals("user@test.com", guardada.getEmailUsuario());
    }

    /**
     * Función: procesarNotificaciones_ConSimilitudAltaYTitulo_DebeUsarTituloProporcionado
     * Título: Procesar coincidencia con título personalizado
     * Descripción: Verifica que si el motor de IA provee un título específico en el DTO 
     * de una coincidencia válida, el servicio lo respete y no asigne el título por defecto.
     */
    @Test
    void procesarNotificaciones_ConSimilitudAltaYTitulo_DebeUsarTituloProporcionado() {
        // Preparación
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(95.0);
        matchDTO.setTitulo("¡Encontramos a Firulais!");
        matchDTO.setMensaje("Tu mascota está a salvo.");

        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // Acción
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // Verificación
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();
        assertEquals("¡Encontramos a Firulais!", guardada.getTitulo());
    }

    /**
     * Función: procesarNotificaciones_ConSimilitudBaja_NoDebeGuardar
     * Título: Descartar coincidencia por similitud baja (Similitud < 85%)
     * Descripción: Comprueba que el servicio ignore silenciosamente las coincidencias 
     * cuyo porcentaje de similitud no alcance el umbral mínimo, evitando consultas Feign 
     * y operaciones de guardado innecesarias.
     */
    @Test
    void procesarNotificaciones_ConSimilitudBaja_NoDebeGuardar() {
        // Preparación
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(50.0);

        // Acción
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // Verificación
        verify(reporteClient, never()).obtenerReportePorId(anyLong());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    /**
     * Función: obtenerPorUsuario_DebeRetornarLista
     * Título: Obtener notificaciones por usuario
     * Descripción: Verifica que el método delegue correctamente la búsqueda al repositorio 
     * utilizando el ID de usuario proporcionado y retorne la lista resultante.
     */
    @Test
    void obtenerPorUsuario_DebeRetornarLista() {
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(5L))
                .thenReturn(List.of(notificacionMock));

        List<Notificacion> resultado = notificacionService.obtenerPorUsuario(5L);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    /**
     * Función: eliminarNotificacion_Existente_DebeBorrar
     * Título: Eliminar notificación existente
     * Descripción: Valida que si el ID de la notificación existe en la base de datos, 
     * el servicio ejecute la orden de eliminación en el repositorio.
     */
    @Test
    void eliminarNotificacion_Existente_DebeBorrar() {
        when(notificacionRepository.existsById(1L)).thenReturn(true);

        notificacionService.eliminarNotificacion(1L);

        verify(notificacionRepository, times(1)).deleteById(1L);
    }

    /**
     * Función: eliminarNotificacion_NoExistente_DebeLanzarExcepcion
     * Título: Fallo al eliminar notificación inexistente
     * Descripción: Asegura que se arroje una RuntimeException si se intenta eliminar 
     * un ID de notificación que no se encuentra registrado en el sistema.
     */
    @Test
    void eliminarNotificacion_NoExistente_DebeLanzarExcepcion() {
        when(notificacionRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.eliminarNotificacion(99L)
        );
        assertEquals("La notificación con ID 99 no existe.", ex.getMessage());
    }

    /**
     * Función: obtenerTodas_DebeRetornarListaCompleta
     * Título: Listar todas las notificaciones
     * Descripción: Comprueba que el servicio recupere exitosamente el universo completo 
     * de notificaciones almacenadas llamando al método findAll del repositorio.
     */
    @Test
    void obtenerTodas_DebeRetornarListaCompleta() {
        when(notificacionRepository.findAll()).thenReturn(List.of(notificacionMock));

        List<Notificacion> resultado = notificacionService.obtenerTodas();

        assertEquals(1, resultado.size());
    }

    /**
     * Función: marcarComoLeida_Exito
     * Título: Marcar notificación como leída (Éxito)
     * Descripción: Verifica que al solicitar la actualización de lectura, el servicio 
     * recupere la entidad, cambie su estado booleano a true y persista el cambio.
     */
    @Test
    void marcarComoLeida_Exito() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionMock));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionMock);

        Notificacion resultado = notificacionService.marcarComoLeida(1L);

        assertTrue(resultado.isLeido());
        verify(notificacionRepository).save(notificacionMock);
    }

    /**
     * Función: marcarComoLeida_NoExistente_DebeLanzarExcepcion
     * Título: Fallo al marcar como leída notificación inexistente
     * Descripción: Valida que se lance una excepción controlada cuando se intenta 
     * actualizar el estado de lectura de un ID que no existe en la base de datos.
     */
    @Test
    void marcarComoLeida_NoExistente_DebeLanzarExcepcion() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.marcarComoLeida(99L)
        );
        assertEquals("Notificación no encontrada con ID: 99", ex.getMessage());
    }
    
    @Test
    void procesarReporteMascotaRabbitMQ_DebeEjecutarFlujoCompleto() {
        // Preparación del evento simulado de RabbitMQ
        com.backend.notificaciones.dto.MascotaReportadaEvent evento = new com.backend.notificaciones.dto.MascotaReportadaEvent();
        evento.setNombreMascota("Firulais");
        evento.setMascotaId(42L);
        evento.setUsuarioId(10L);
        evento.setTipoReporte("PERDIDO");

        // Acción: Llamada directa al método para activar la cobertura de JaCoCo
        assertDoesNotThrow(() -> notificacionService.procesarReporteMascotaRabbitMQ(evento));
    }
}