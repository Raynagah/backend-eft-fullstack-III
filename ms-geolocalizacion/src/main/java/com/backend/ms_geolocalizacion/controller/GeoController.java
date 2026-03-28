package com.backend.ms_geolocalizacion.controller;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geolocalizacion")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    // 1. Registrar una nueva ubicación (POST /api/geolocalizacion)
    @PostMapping
    public ResponseEntity<UbicacionAlerta> registrar(@RequestBody UbicacionAlerta ubicacion) {
        UbicacionAlerta nueva = geoService.registrarUbicacion(ubicacion);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    // 2. Obtener todas las ubicaciones registradas (GET /api/geolocalizacion)
    @GetMapping
    public ResponseEntity<List<UbicacionAlerta>> obtenerTodas() {
        return ResponseEntity.ok(geoService.obtenerTodas());
    }

    // 3. ¡EL MÁS IMPORTANTE! Buscar por radio (GET /api/geolocalizacion/cercanos)
    @GetMapping("/cercanos")
    public ResponseEntity<List<UbicacionAlerta>> buscarCercanas(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radio) {

        List<UbicacionAlerta> cercanas = geoService.buscarCercanas(lat, lon, radio);
        return ResponseEntity.ok(cercanas);
    }
}