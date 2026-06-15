package com.backend.bff.service;

import com.backend.bff.client.NotificacionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BffNotificacionService {

    private final NotificacionClient notificacionClient;

    public Object obtenerMisNotificaciones(Long usuarioId) {
        // Delegamos la llamada al microservicio a través de Feign
        return notificacionClient.obtenerPorUsuario(usuarioId);
    }

    public Object marcarComoLeida(Long id) {
        // Actualizamos el estado en el microservicio
        return notificacionClient.marcarComoLeida(id);
    }

    public void eliminarNotificacion(Long id) {
        notificacionClient.eliminarNotificacion(id);
    }
}