package com.backend.gestionMascotas.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.gestionMascotas.client.GeolocalizacionClient;
import com.backend.gestionMascotas.config.RabbitMQConfig;
import com.backend.gestionMascotas.dto.MascotaReportadaEvent;
import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.dto.ReporteResponseDTO;
import com.backend.gestionMascotas.exception.ReporteNotFoundException;
import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.repository.MascotaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Función: MascotaService (Servicio)
 * Título: Servicio de Gestión de Mascotas
 * Descripción: Gestiona la lógica de negocio central de los reportes de mascotas. Orquesta la persistencia, las transacciones distribuidas (Saga) comunicándose con el microservicio de geolocalización, emite eventos asíncronos vía RabbitMQ y administra el almacenamiento en caché para optimizar consultas.
 */
@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ReporteFactory reporteFactory;
    private final GeolocalizacionClient geoClient;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Función: registrarReporte
     * Título: Registrar y procesar reporte
     * Descripción: Crea y guarda un nuevo reporte utilizando el patrón Factory. Inicia una transacción distribuida registrando la ubicación en el microservicio de geolocalización y, si es exitosa, emite un evento en RabbitMQ. Si ocurre un fallo en los servicios externos, ejecuta una compensación automática. Limpia las cachés de listas.
     *
     * @param dto Objeto ReporteRequestDTO con la información de la mascota a reportar.
     * @return El objeto ReporteResponseDTO del reporte recién persistido.
     */
    @CacheEvict(value = {"mascotas_lista", "mascotas_tipo"}, allEntries = true)
    public ReporteResponseDTO registrarReporte(ReporteRequestDTO dto) {
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(dto);
        nuevoReporte = mascotaRepository.save(nuevoReporte);

        try {
            Map<String, Object> datosGeo = new HashMap<>();
            datosGeo.put("reporteId", nuevoReporte.getId());
            datosGeo.put("latitud", nuevoReporte.getLatitud());
            datosGeo.put("longitud", nuevoReporte.getLongitud());
            datosGeo.put("tipoAlerta", nuevoReporte.getTipoReporte());

            geoClient.registrarUbicacion(datosGeo);

            nuevoReporte.setSagaStatus("COMPLETED");
            mascotaRepository.save(nuevoReporte);

            // Disparador de mensajería RabbitMQ

            MascotaReportadaEvent evento = new MascotaReportadaEvent(
                    nuevoReporte.getId(),
                    nuevoReporte.getUsuarioId(),
                    nuevoReporte.getNombre(),
                    nuevoReporte.getTipoReporte()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, evento);

        } catch (Exception e) {
            this.compensarReporte(nuevoReporte.getId());
        }

        return convertirADTO(nuevoReporte);
    }

    /**
     * Función: compensarReporte
     * Título: Compensar estado del reporte
     * Descripción: Actualiza el estado del reporte a 'FAILED_SYNC' en la base de datos si ocurre un error durante el flujo de la saga (ej. fallo al registrar la geolocalización o emitir el mensaje).
     *
     * @param id Identificador único de tipo Long del reporte a compensar.
     */
    public void compensarReporte(Long id) {
        mascotaRepository.findById(id).ifPresent(m -> {
            m.setSagaStatus("FAILED_SYNC");
            mascotaRepository.save(m);
        });
    }

    /**
     * Función: eliminarReporte
     * Título: Eliminar reporte y geolocalización
     * Descripción: Elimina el reporte de la base de datos local e intenta borrar la ubicación vinculada en el microservicio de geolocalización. Adicionalmente, invalida todas las cachés relacionadas a mascotas.
     *
     * @param id Identificador único de tipo Long del reporte a eliminar.
     * @throws ReporteNotFoundException Si no se encuentra un reporte asociado al ID proporcionado.
     */
    @Transactional
    @CacheEvict(value = {"mascotas_lista", "mascotas_tipo", "mascota_detalle"}, allEntries = true)
    public void eliminarReporte(Long id) {
        ReporteMascota reporte = mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));

        try {
            geoClient.eliminarUbicacion(id);
        } catch (Exception e) {
            System.err.println("No se pudo eliminar la geolocalización: " + e.getMessage());
        }

        mascotaRepository.delete(reporte);
    }

    /**
     * Función: obtenerTodosLosReportes
     * Título: Obtener lista completa de reportes
     * Descripción: Recupera todos los reportes de la base de datos, los transforma en DTOs y almacena en caché el resultado para optimizar la carga del feed principal.
     *
     * @return Lista completa de objetos ReporteResponseDTO.
     */
    @Cacheable(value = "mascotas_lista")
    public List<ReporteResponseDTO> obtenerTodosLosReportes() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Función: obtenerReportesPorTipo
     * Título: Obtener reportes por categoría
     * Descripción: Filtra y devuelve los reportes según su tipo (ej. "PERDIDA" o "ENCONTRADA"), almacenando el resultado en caché utilizando el tipo como clave.
     *
     * @param tipoReporte Cadena de texto (String) con el tipo de reporte a filtrar.
     * @return Lista de objetos ReporteResponseDTO filtrada por el tipo especificado.
     */
    @Cacheable(value = "mascotas_tipo", key = "#tipoReporte")
    public List<ReporteResponseDTO> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Función: obtenerReportePorId
     * Título: Obtener detalle de reporte
     * Descripción: Consulta un reporte específico por su ID y guarda el resultado en caché para agilizar las vistas de detalle.
     *
     * @param id Identificador único de tipo Long del reporte deseado.
     * @return Objeto ReporteResponseDTO con los detalles del reporte.
     * @throws ReporteNotFoundException Si el identificador no existe en el sistema.
     */
    @Cacheable(value = "mascota_detalle", key = "#id")
    public ReporteResponseDTO obtenerReportePorId(Long id) {
        ReporteMascota entidad = mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        return convertirADTO(entidad);
    }

    /**
     * Función: convertirADTO
     * Título: Convertir entidad a DTO
     * Descripción: Método utilitario privado para mapear los atributos de la entidad de dominio ReporteMascota hacia el objeto de transferencia de datos ReporteResponseDTO.
     *
     * @param entidad Objeto ReporteMascota persistido en la base de datos.
     * @return Objeto ReporteResponseDTO estructurado para la respuesta al cliente.
     */
    private ReporteResponseDTO convertirADTO(ReporteMascota entidad) {
        return new ReporteResponseDTO(
                entidad.getId(),
                entidad.getUsuarioId(),
                entidad.getTipoReporte(),
                entidad.getNombre(),
                entidad.getEspecie(),
                entidad.getRaza(),
                entidad.getColor(),
                entidad.getTamano(),
                entidad.getNombreContacto(),
                entidad.getTelefonoContacto(),
                entidad.getEmailContacto(),
                entidad.getFotografiaUrl(),
                entidad.getLatitud(),
                entidad.getLongitud(),
                entidad.getFechaReporte(),
                entidad.getSagaStatus()
        );
    }
}