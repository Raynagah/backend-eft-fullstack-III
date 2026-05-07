package com.backend.gestionMascotas.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para DB

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

    public ReporteMascota registrarReporte(ReporteRequestDTO dto) {
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

        return nuevoReporte;
    }

    public void compensarReporte(Long id) {
        mascotaRepository.findById(id).ifPresent(m -> {
            m.setSagaStatus("FAILED_SYNC");
            mascotaRepository.save(m);
        });
    }

    // --- NUEVO MÉTODO: Eliminar Reporte ---
    @Transactional
    public void eliminarReporte(Long id) {
        ReporteMascota reporte = mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));

        // 1. Intentar eliminar en el microservicio de Geolocalización (limpieza)
        try {
            geoClient.eliminarUbicacion(id);
            // Nota: Asegúrate que tu Feign Client 'GeolocalizacionClient' tenga el método @DeleteMapping("/{id}")
        } catch (Exception e) {
            System.err.println("No se pudo eliminar la geolocalización, puede que no existiera: " + e.getMessage());
        }

        // 2. Eliminar en local
        mascotaRepository.delete(reporte);
    }

    public List<ReporteMascota> obtenerTodosLosReportes() {
        return mascotaRepository.findAll();
    }

    public List<ReporteMascota> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte);
    }

    public ReporteMascota obtenerReportePorId(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
    }
}