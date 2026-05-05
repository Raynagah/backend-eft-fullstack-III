package com.backend.notificaciones.service;

import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public void procesarNotificaciones(List<NotificacionMatchDTO> coincidencias) {
        // Filtrar solo las que tengan 85% o más
        coincidencias.stream()
                .filter(c -> c.getPorcentajeSimilitud() >= 85.0)
                .forEach(this::guardarYNotificar);
    }

    private void guardarYNotificar(NotificacionMatchDTO dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setReporteId(dto.getReporteId());
        notificacion.setEmailUsuario(dto.getEmailUsuario());
        notificacion.setPorcentajeSimilitud(dto.getPorcentajeSimilitud());
        notificacion.setTitulo(dto.getTitulo() != null ? dto.getTitulo() : "¡Posible coincidencia!");
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setFotografiaUrl(dto.getFotografiaUrl());
        
        // Guardar en la base de datos de Neon
        notificacionRepository.save(notificacion);

        // Log para auditoría
        System.out.println("ALERTA REGISTRADA: Match del " + dto.getPorcentajeSimilitud() + 
                           "% para el usuario: " + dto.getEmailUsuario());
    }

    public List<Notificacion> obtenerPorUsuario(String email) {
        return notificacionRepository.findByEmailUsuarioOrderByFechaCreacionDesc(email);
    }
}