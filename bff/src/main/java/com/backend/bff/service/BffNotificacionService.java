package com.backend.bff.service;

import com.backend.bff.client.NotificacionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Función: BffNotificacionService (Servicio)
 * Título: Servicio de Gestión de Notificaciones (BFF)
 * Descripción: Actúa como la capa de servicio intermedia en el Backend For Frontend para gestionar el flujo de notificaciones. Su responsabilidad principal es delegar las operaciones de consulta, actualización y eliminación hacia el microservicio correspondiente mediante Feign Client.
 */
@Service
@RequiredArgsConstructor
public class BffNotificacionService {

    private final NotificacionClient notificacionClient;

    /**
     * Función: obtenerMisNotificaciones
     * Título: Obtener notificaciones por usuario
     * Descripción: Consulta al microservicio de notificaciones para obtener el listado completo de alertas asociadas a un usuario específico.
     *
     * @param usuarioId Identificador único de tipo Long del usuario cuyas notificaciones se desean recuperar.
     * @return Objeto (generalmente una lista) con la información de las notificaciones encontradas.
     */
    public Object obtenerMisNotificaciones(Long usuarioId) {
        // Delegamos la llamada al microservicio a través de Feign
        return notificacionClient.obtenerPorUsuario(usuarioId);
    }

    /**
     * Función: marcarComoLeida
     * Título: Marcar notificación como leída
     * Descripción: Envía una solicitud al microservicio para actualizar el estado de una notificación específica, marcándola como leída en el sistema.
     *
     * @param id Identificador único de tipo Long de la notificación a actualizar.
     * @return Objeto con el resultado de la operación de actualización.
     */
    public Object marcarComoLeida(Long id) {
        // Actualizamos el estado en el microservicio
        return notificacionClient.marcarComoLeida(id);
    }

    /**
     * Función: eliminarNotificacion
     * Título: Eliminar notificación
     * Descripción: Delega la eliminación permanente de una notificación específica identificada por su ID hacia el microservicio encargado.
     *
     * @param id Identificador único de tipo Long de la notificación que se desea eliminar.
     */
    public void eliminarNotificacion(Long id) {
        notificacionClient.eliminarNotificacion(id);
    }
}