package com.backend.bff.service;

import com.backend.bff.client.NotificacionClient;
import com.backend.bff.dto.NotificacionRequestDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

/**
 * Función: BffNotificacionServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Notificaciones (BFF)
 * Descripción: Modulo de pruebas encargado de validar la correcta interacción 
 * y delegación de operaciones entre el BFF y el microservicio core de notificaciones 
 * utilizando Mockito para aislar las peticiones HTTP de Feign.
 */
@ExtendWith(MockitoExtension.class)
class BffNotificacionServiceTest {

    @Mock
    private NotificacionClient notificacionClient;

    @InjectMocks
    private BffNotificacionService service;

    /**
     * Función: obtenerMisNotificaciones_DebeRetornarRespuestaDelCliente
     * Título: Validar obtención de notificaciones por usuario
     * Descripción: Comprueba que al solicitar las notificaciones de un usuario, el BFF 
     * delegue correctamente la consulta al Feign Client y devuelva la lista tipada de DTOs.
     */
    @Test
    void obtenerMisNotificaciones_DebeRetornarRespuestaDelCliente() {
        Long usuarioId = 42L;
        // Corregido: Inicializamos la lista con el tipo esperado por el cliente Feign
        List<NotificacionRequestDTO> mockRespuesta = List.of(new NotificacionRequestDTO());
        
        when(notificacionClient.obtenerPorUsuario(usuarioId)).thenReturn(mockRespuesta);

        Object resultado = service.obtenerMisNotificaciones(usuarioId);

        assertNotNull(resultado);
        assertEquals(mockRespuesta, resultado);
        verify(notificacionClient, times(1)).obtenerPorUsuario(usuarioId);
    }

    /**
     * Función: marcarComoLeida_DebeRetornarRespuestaDelCliente
     * Título: Validar cambio de estado a leída
     * Descripción: Asegura que la petición para marcar una notificación como leída se envíe 
     * al microservicio con el ID correcto y retorne el DTO de la notificación modificada.
     */
    @Test
    void marcarComoLeida_DebeRetornarRespuestaDelCliente() {
        Long notificacionId = 100L;
        // Corregido: Instanciamos el objeto con el tipo esperado por el cliente Feign
        NotificacionRequestDTO mockRespuestaActualizada = new NotificacionRequestDTO();
        
        when(notificacionClient.marcarComoLeida(notificacionId)).thenReturn(mockRespuestaActualizada);

        Object resultado = service.marcarComoLeida(notificacionId);

        assertNotNull(resultado);
        assertEquals(mockRespuestaActualizada, resultado);
        verify(notificacionClient, times(1)).marcarComoLeida(notificacionId);
    }

    /**
     * Función: eliminarNotificacion_DebeLlamarAlCliente
     * Título: Validar eliminación de notificación
     * Descripción: Verifica que el método de eliminación (que no retorna valor) invoque 
     * al cliente Feign exactamente una vez con el identificador proporcionado.
     */
    @Test
    void eliminarNotificacion_DebeLlamarAlCliente() {
        Long notificacionId = 500L;

        // Ejecutamos la acción void
        service.eliminarNotificacion(notificacionId);

        // Verificamos que se haya delegado la orden de borrado al cliente
        verify(notificacionClient, times(1)).eliminarNotificacion(notificacionId);
    }
}