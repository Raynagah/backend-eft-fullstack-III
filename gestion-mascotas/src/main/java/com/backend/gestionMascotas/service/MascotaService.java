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

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ReporteFactory reporteFactory;
    private final GeolocalizacionClient geoClient;
    private final RabbitTemplate rabbitTemplate;

    // Limpia las listas cacheadas porque hay un registro nuevo. Usamos allEntries = true para borrar todas las listas guardadas y evitar mostrar datos viejos.
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

    public void compensarReporte(Long id) {
        mascotaRepository.findById(id).ifPresent(m -> {
            m.setSagaStatus("FAILED_SYNC");
            mascotaRepository.save(m);
        });
    }

    // Limpia todas las cachés asociadas a mascotas al eliminar una
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

    // Guarda la lista completa en RAM, es ideal porque es la consulta que más harán los usuarios al entrar al feed.
    @Cacheable(value = "mascotas_lista")
    public List<ReporteResponseDTO> obtenerTodosLosReportes() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Guarda listas filtradas por "PERDIDA" o "ENCONTRADA", se usa 'key' para separar la caché según el tipo que pide el usuario.
    @Cacheable(value = "mascotas_tipo", key = "#tipoReporte")
    public List<ReporteResponseDTO> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Guarda el detalle de un solo reporte, ayuda a cargar rápido cuando alguien entra a ver los detalles y foto del animal.
    @Cacheable(value = "mascota_detalle", key = "#id")
    public ReporteResponseDTO obtenerReportePorId(Long id) {
        ReporteMascota entidad = mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        return convertirADTO(entidad);
    }

    // Metodo utilitario para convertir Entidad a DTO
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