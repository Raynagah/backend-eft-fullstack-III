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

/**
 * Función: NotificacionService (Servicio)
 * Título: Servicio de Gestión de Notificaciones
 * Descripción: Servicio principal para gestionar las notificaciones, incluyendo la lógica de negocio para procesar coincidencias detectadas por el motor de IA, interactuar con el microservicio de reportes mediante clientes Feign y manejar las operaciones CRUD de las notificaciones.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {
    // Servicio principal para gestionar las notificaciones, incluyendo la lógica de negocio para procesar
    // coincidencias detectadas por el motor de IA, interactuar con el microservicio de reportes y manejar
    // las operaciones CRUD de las notificaciones

    private final NotificacionRepository notificacionRepository;
    private final ReporteClient reporteClient; // Cliente Feign inyectado

    /**
     * Función: procesarNotificaciones
     * Título: Procesar coincidencias de IA
     * Descripción: Recibe y evalúa un listado de posibles coincidencias. Solo procesa y genera notificaciones para aquellas cuyo porcentaje de similitud es igual o superior al 85%.
     *
     * @param coincidencias Lista de objetos NotificacionMatchDTO proporcionados por el motor de IA.
     */
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

    /**
     * Función: guardarYNotificar
     * Título: Guardar y notificar coincidencia
     * Descripción: Método interno que consulta el microservicio de reportes a través de Feign para obtener la información del reporte original, mapea los datos y persiste la nueva notificación en la base de datos.
     *
     * @param dto Objeto NotificacionMatchDTO con los datos de la coincidencia procesada.
     */
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

    /**
     * Función: obtenerPorUsuario
     * Título: Obtener notificaciones de usuario
     * Descripción: Consulta la base de datos para recuperar todas las notificaciones asociadas a un ID de usuario específico, ordenándolas por fecha de creación de forma descendente.
     *
     * @param usuarioId Identificador único de tipo Long del usuario a consultar.
     * @return Lista de objetos Notificacion pertenecientes al usuario.
     */
    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    /**
     * Función: eliminarNotificacion
     * Título: Eliminar notificación
     * Descripción: Verifica la existencia de una notificación mediante su ID y procede a eliminarla permanentemente de la base de datos. Lanza una excepción si el ID no existe.
     *
     * @param id Identificador único de tipo Long de la notificación a eliminar.
     */
    public void eliminarNotificacion(Long id) {
        if (!notificacionRepository.existsById(id)) {
            throw new RuntimeException("La notificación con ID " + id + " no existe.");
        }
        notificacionRepository.deleteById(id);
    }

    /**
     * Función: obtenerTodas
     * Título: Obtener todas las notificaciones
     * Descripción: Retorna el registro completo de notificaciones en el sistema sin aplicar ningún filtro, útil para paneles de administración o debugging.
     *
     * @return Lista completa de objetos Notificacion almacenados en la base de datos.
     */
    public List<Notificacion> obtenerTodas() {
        return notificacionRepository.findAll();
    }

    /**
     * Función: marcarComoLeida
     * Título: Marcar notificación como leída
     * Descripción: Busca una notificación por su identificador y modifica su estado de lectura a verdadero (true), guardando el cambio de forma transaccional.
     *
     * @param id Identificador único de tipo Long de la notificación a actualizar.
     * @return Objeto Notificacion con su estado de lectura actualizado.
     */
    @Transactional
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        notificacion.setLeido(true);
        return notificacionRepository.save(notificacion);
    }

    /**
     * Función: procesarReporteMascotaRabbitMQ
     * Título: Procesar evento de mascota (RabbitMQ)
     * Descripción: Listener asíncrono que se suscribe a una cola de RabbitMQ para interceptar e imprimir eventos relacionados con nuevos reportes de mascotas en el ecosistema de microservicios.
     *
     * @param evento Objeto MascotaReportadaEvent que contiene los detalles emitidos por el broker de mensajería.
     */
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
    }
}