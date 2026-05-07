package com.backend.ms_geolocalizacion.service;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final UbicacionAlertaRepository repository;
    private static final double RADIO_TIERRA_KM = 6371.0;

    public UbicacionAlerta registrarUbicacion(UbicacionAlerta ubicacion) {
        return repository.save(ubicacion);
    }

    public List<UbicacionAlerta> obtenerTodas() {
        return repository.findAll();
    }

    public List<UbicacionAlerta> buscarCercanas(Double miLat, Double miLon, Double radioMaxKm) {
        List<UbicacionAlerta> todas = repository.findAll();
        return todas.stream()
                .filter(u -> calcularDistanciaKm(miLat, miLon, u.getLatitud(), u.getLongitud()) <= radioMaxKm)
                .collect(Collectors.toList());
    }

    public boolean eliminarUbicacion(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
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