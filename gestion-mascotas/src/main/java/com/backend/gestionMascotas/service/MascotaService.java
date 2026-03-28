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

    // METODO UNIFICADO: Crea, Guarda y Sincroniza
    public ReporteMascota registrarReporte(String tipo, String especie, String raza, String color, String tamano,
                                           String nombre, String telefono, String email, String fotoUrl,
                                           Double lat, Double lng) {

        // 1. Usamos el Factory Method para crear la entidad (como lo tenías antes)
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(
                tipo, especie, raza, color, tamano, nombre, telefono, email, fotoUrl, lat, lng
        );

        // 2. Guardamos en la base de datos de Mascotas
        nuevoReporte = mascotaRepository.save(nuevoReporte);

        // 3. ENVIAR A GEOLOCALIZACIÓN (La comunicación entre microservicios)
        try {
            Map<String, Object> datosGeo = new HashMap<>();
            datosGeo.put("reporteId", nuevoReporte.getId());
            datosGeo.put("latitud", nuevoReporte.getLatitud());
            datosGeo.put("longitud", nuevoReporte.getLongitud());
            datosGeo.put("tipoAlerta", nuevoReporte.getTipoReporte());

            geoClient.registrarUbicacion(datosGeo);
            System.out.println("Sincronización exitosa con ms-geolocalizacion");
        } catch (Exception e) {
            // Si falla el microservicio de geo, el reporte de mascota IGUAL se guarda.
            // Esto se llama "Resiliencia": un fallo en B no mata a A.
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
}