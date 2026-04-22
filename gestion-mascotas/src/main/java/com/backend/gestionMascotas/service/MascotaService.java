package com.backend.gestionMascotas.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

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

        // 1. Usamos el factory pasando el DTO completo
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(dto);

        // 2. Guardamos en nuestra base de datos (Neon)
        nuevoReporte = mascotaRepository.save(nuevoReporte);

        // 3. Sincronización asíncrona (simulada con try-catch) con ms-geolocalizacion
        try {
            Map<String, Object> datosGeo = new HashMap<>();
            datosGeo.put("reporteId", nuevoReporte.getId());
            datosGeo.put("latitud", nuevoReporte.getLatitud());
            datosGeo.put("longitud", nuevoReporte.getLongitud());
            datosGeo.put("tipoAlerta", nuevoReporte.getTipoReporte());

            geoClient.registrarUbicacion(datosGeo);
            System.out.println("Sincronización exitosa con ms-geolocalizacion");
        } catch (Exception e) {
            // Logueamos el error pero no bloqueamos la respuesta al usuario
            System.err.println("Error al sincronizar con geolocalización: " + e.getMessage());
        }

        return nuevoReporte;
    }

    public List<ReporteMascota> obtenerTodosLosReportes() {
        return mascotaRepository.findAll();
    }

    public List<ReporteMascota> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte);
    }

    public ReporteMascota obtenerReportePorId(Long id) {
        // Usamos nuestra excepción personalizada para que el Handler devuelva un 404
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ReporteNotFoundException(id));
    }
}