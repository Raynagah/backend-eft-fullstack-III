package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.client.GeolocalizacionClient;
import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ReporteFactory reporteFactory;
    private final GeolocalizacionClient geoClient;

    
    public ReporteMascota registrarReporte(String tipo, String especie, String raza, String color, String tamano,
                                           String nombre, String telefono, String email, String fotoUrl,
                                           Double lat, Double lng) {

        
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(
                tipo, especie, raza, color, tamano, nombre, telefono, email, fotoUrl, lat, lng
        );


        nuevoReporte = mascotaRepository.save(nuevoReporte);

        
        try {
            Map<String, Object> datosGeo = new HashMap<>();
            datosGeo.put("reporteId", nuevoReporte.getId());
            datosGeo.put("latitud", nuevoReporte.getLatitud());
            datosGeo.put("longitud", nuevoReporte.getLongitud());
            datosGeo.put("tipoAlerta", nuevoReporte.getTipoReporte());

            geoClient.registrarUbicacion(datosGeo);
            System.out.println("Sincronización exitosa con ms-geolocalizacion");
        } catch (Exception e) {
            
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

    // Metodo para buscar una mascota por su ID
    public ReporteMascota obtenerReportePorId(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte de mascota no encontrado con ID: " + id));
    }
}
