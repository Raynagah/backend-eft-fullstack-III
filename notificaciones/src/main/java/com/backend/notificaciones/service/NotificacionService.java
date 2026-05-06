package com.backend.notificaciones.service;

import com.backend.notificaciones.client.ReporteClient;
import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;
import com.backend.notificaciones.dto.ReporteRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ReporteClient reporteClient; // Cliente Feign inyectado

    public void procesarNotificaciones(List<NotificacionMatchDTO> coincidencias) {
        System.out.println("Recibidas " + coincidencias.size() + " posibles coincidencias");

        coincidencias.forEach(c -> {
            System.out.println("Evaluando reporte ID: " + c.getReporteId() + " con similitud: " + c.getPorcentajeSimilitud());
            if (c.getPorcentajeSimilitud() >= 85.0) {
                guardarYNotificar(c);
            } else {
                System.out.println("Similitud insuficiente para guardar.");
            }
        });
    }

    private void guardarYNotificar(NotificacionMatchDTO dto) {
        // LLAMADA FEIGN: Obtenemos los datos del reporte original
        // Suponiendo que el endpoint de reportes devuelve el DTO que mostraste
        ReporteRequestDTO reporteOriginal = reporteClient.obtenerReportePorId(dto.getReporteId());

        Notificacion notificacion = new Notificacion();
        notificacion.setReporteId(dto.getReporteId());

        // Seteamos los datos obtenidos del Microservicio de Reportes
        notificacion.setUsuarioId(reporteOriginal.getUsuarioId());
        notificacion.setEmailUsuario(reporteOriginal.getEmailContacto());

        notificacion.setPorcentajeSimilitud(dto.getPorcentajeSimilitud());
        notificacion.setTitulo(dto.getTitulo() != null ? dto.getTitulo() : "¡Posible coincidencia!");
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setFotografiaUrl(dto.getFotografiaUrl());

        notificacionRepository.save(notificacion);

        System.out.println("NOTIFICACIÓN CREADA para Usuario ID: " + reporteOriginal.getUsuarioId());
    }

    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public void eliminarNotificacion(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("La notificación con ID " + id + " no existe.");
        }
        notificacionRepository.deleteById(id);
    }

    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    // --- Marcar como leída ---
    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }
}