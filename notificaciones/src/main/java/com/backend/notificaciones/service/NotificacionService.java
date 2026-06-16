package com.backend.notificaciones.service;

import com.backend.notificaciones.client.ReporteClient;
import com.backend.notificaciones.config.RabbitMQConfig;
import com.backend.notificaciones.dto.MascotaReportadaEvent;
import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;
import com.backend.notificaciones.dto.ReporteRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {
    // Servicio principal para gestionar las notificaciones, incluyendo la lógica de negocio para procesar
    // coincidencias detectadas por el motor de IA, interactuar con el microservicio de reportes y manejar
    // las operaciones CRUD de las notificaciones

    private final NotificacionRepository notificacionRepository;
    private final ReporteClient reporteClient; // Cliente Feign inyectado

    // Metodo para procesar las coincidencias recibidas del motor de IA, creando notificaciones
    // solo si el porcentaje de similitud es mayor o igual al 85%
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

    // Metodo privado para guardar la notificación en la base de datos y realizar la llamada Feign para obtener los datos del reporte original
    private void guardarYNotificar(NotificacionMatchDTO dto) {
        // LLAMADA FEIGN: Obtenemos los datos del reporte original utilizando el ID del reporte que viene en el DTO de coincidencia
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

    // Metodo para obtener todas las notificaciones de un usuario específico, utilizando su ID como parámetro de búsqueda
    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    // Metodo para eliminar una notificación específica por su ID, eliminándola de la base de datos
    public void eliminarNotificacion(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("La notificación con ID " + id + " no existe.");
        }
        notificacionRepository.deleteById(id);
    }

    // Metodo para obtener todas las notificaciones, principalmente para pruebas y administración, sin filtros específicos
    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    // Metodo para marcar una notificación como leída, cambiando su estado a true en la variable 'leido'
    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }

    // LISTENER DE RABBITMQ - RECIBE EVENTOS ASÍNCRONOS
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES)
    public void procesarReporteMascotaRabbitMQ(MascotaReportadaEvent evento) {
        System.out.println("=========================================================");
        System.out.println("¡RABBITMQ: NUEVO REPORTE DE MASCOTA RECIBIDO!");
        System.out.println("Procesando evento para: " + evento.getNombreMascota());
        System.out.println("ID de Mascota: " + evento.getMascotaId());
        System.out.println("ID de Usuario: " + evento.getUsuarioId());
        System.out.println("Tipo de Reporte: " + evento.getTipoReporte());
        System.out.println("=========================================================");

        // Aquí puedes reutilizar la lógica para crear una notificación genérica
        // o guardar un registro si lo consideras necesario para el sistema de alertas.
    }
}