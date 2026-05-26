package com.backend.gestionMascotas.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <-- Agregamos esta importación

import com.backend.gestionMascotas.dto.ReporteResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.gestionMascotas.client.GeolocalizacionClient;
import com.backend.gestionMascotas.dto.ReporteRequestDTO;
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

    // ✅ AHORA DEVUELVE DTO
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
        } catch (Exception e) {
            this.compensarReporte(nuevoReporte.getId());
        }

        return convertirADTO(nuevoReporte); // <-- Usamos el conversor aquí
    }

    public void compensarReporte(Long id) {
        mascotaRepository.findById(id).ifPresent(m -> {
            m.setSagaStatus("FAILED_SYNC");
            mascotaRepository.save(m);
        });
    }

    @Transactional
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

    // ✅ AHORA DEVUELVE LISTA DE DTOs
    public List<ReporteResponseDTO> obtenerTodosLosReportes() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::convertirADTO) // <-- Convierte cada entidad de la lista a DTO
                .collect(Collectors.toList());
    }

    // ✅ AHORA DEVUELVE LISTA DE DTOs
    public List<ReporteResponseDTO> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte)
                .stream()
                .map(this::convertirADTO) // <-- Convierte cada entidad de la lista a DTO
                .collect(Collectors.toList());
    }

    // ✅ AHORA DEVUELVE DTO
    public ReporteResponseDTO obtenerReportePorId(Long id) {
        ReporteMascota entidad = mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
        return convertirADTO(entidad); // <-- Convertimos antes de retornar
    }

    // Métodoo utilitario para convertir Entidad a DTO
    private ReporteResponseDTO convertirADTO(ReporteMascota entidad) {
        return new ReporteResponseDTO(
                entidad.getId(),
                entidad.getTipoReporte(),
                entidad.getNombre(),
                entidad.getEspecie(),
                entidad.getRaza(),
                entidad.getColor(),
                entidad.getTamano(),
                entidad.getNombreContacto(),
                entidad.getTelefonoContacto(),
                entidad.getFotografiaUrl(),
                entidad.getLatitud(),
                entidad.getLongitud(),
                entidad.getFechaReporte(),
                entidad.getSagaStatus()
        );
    }
}