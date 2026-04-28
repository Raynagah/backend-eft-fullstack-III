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

        // 1. Usamos el factory pasando el DTO completo (El factory debe estar mapeando el usuarioId)
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(dto);

        // 2. Guardamos en nuestra base de datos (Neon)
        // Al guardar, el @PrePersist de la entidad dejará sagaStatus en "PENDING"
        nuevoReporte = mascotaRepository.save(nuevoReporte);

        // 3. Sincronización con ms-geolocalizacion (Paso de la Saga)
        try {
            Map<String, Object> datosGeo = new HashMap<>();
            datosGeo.put("reporteId", nuevoReporte.getId());
            datosGeo.put("latitud", nuevoReporte.getLatitud());
            datosGeo.put("longitud", nuevoReporte.getLongitud());
            datosGeo.put("tipoAlerta", nuevoReporte.getTipoReporte());

            // Llamada síncrona al MS Geo
            geoClient.registrarUbicacion(datosGeo);

            // en caso de exito: La Saga se completó correctamente
            nuevoReporte.setSagaStatus("COMPLETED");
            mascotaRepository.save(nuevoReporte);
            System.out.println("Sincronización exitosa con ms-geolocalizacion. Saga Completada.");

        } catch (Exception e) {
            // En caso de fallar ejecutamos la Transacción Compensatoria
            System.err.println("Error al sincronizar con geolocalización. Iniciando compensación... " + e.getMessage());
            this.compensarReporte(nuevoReporte.getId());

            // Opcional: Podríamos lanzar una excepción aquí si queremos que el Gateway
            // le avise al frontend que hubo un error y el reporte no se publicó xd.
        }

        return nuevoReporte;
    }

    // Metodo SAGA
    // Este metodo deshace los cambios si el MS-Geolocalización falla
    public void compensarReporte(Long id) {
        mascotaRepository.findById(id).ifPresent(m -> {
            m.setSagaStatus("FAILED_SYNC"); // Marcamos el reporte como inválido
            mascotaRepository.save(m);
            System.out.println("Transacción compensatoria ejecutada para el reporte ID: " + id);
        });
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