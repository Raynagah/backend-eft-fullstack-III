package com.backend.ms_geolocalizacion.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.ms_geolocalizacion.exception.BadRequestException;
import com.backend.ms_geolocalizacion.exception.ResourceNotFoundException;
import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final UbicacionAlertaRepository repository;
    private static final double RADIO_TIERRA_KM = 6371.0;

    public UbicacionAlerta registrarUbicacion(UbicacionAlerta ubicacion) {
        // Validación de datos requeridos
        if (ubicacion.getReporteId() == null) {
            throw new BadRequestException("El ID de reporte es obligatorio.");
        }
        validarCoordenadas(ubicacion.getLatitud(), ubicacion.getLongitud());
        
        return repository.save(ubicacion);
    }

    public List<UbicacionAlerta> obtenerTodas() {
        return repository.findAll();
    }

    public List<UbicacionAlerta> buscarCercanas(Double miLat, Double miLon, Double radioMaxKm) {
        validarCoordenadas(miLat, miLon);
        if (radioMaxKm == null || radioMaxKm <= 0) {
            throw new BadRequestException("El radio de búsqueda debe ser mayor a 0 KM.");
        }

        List<UbicacionAlerta> todas = repository.findAll();
        return todas.stream()
                .filter(u -> calcularDistanciaKm(miLat, miLon, u.getLatitud(), u.getLongitud()) <= radioMaxKm)
                .collect(Collectors.toList());
    }

    public void eliminarUbicacion(Long id) {
        UbicacionAlerta ubicacion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ninguna ubicación con el ID: " + id));
        repository.delete(ubicacion);
    }

    public UbicacionAlerta obtenerPorReporteId(Long reporteId) {
        return repository.findByReporteId(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ninguna ubicación asociada al reporte con ID: " + reporteId));
    }

    // Método utilitario privado para centralizar la validación matemática de coordenadas
    private void validarCoordenadas(Double lat, Double lon) {
        if (lat == null || lat < -90.0 || lat > 90.0) {
            throw new BadRequestException("La latitud debe ser un valor numérico válido entre -90.0 y 90.0.");
        }
        if (lon == null || lon < -180.0 || lon > 180.0) {
            throw new BadRequestException("La longitud debe ser un valor numérico válido entre -180.0 y 180.0.");
        }
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}