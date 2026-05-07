package com.backend.ms_geolocalizacion.controller;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geolocalizacion")
@RequiredArgsConstructor
@Tag(name = "Geolocalización", description = "Servicios para la gestión de ubicaciones de mascotas")
public class GeoController {

    private final GeoService geoService;

    @PostMapping
    @Operation(summary = "Registrar ubicación", description = "Guarda las coordenadas de una mascota perdida o encontrada")
    public ResponseEntity<UbicacionAlerta> registrar(@RequestBody UbicacionAlerta ubicacion) {
        UbicacionAlerta nueva = geoService.registrarUbicacion(ubicacion);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener todas", description = "Retorna una lista de todas las ubicaciones registradas")
    public ResponseEntity<List<UbicacionAlerta>> obtenerTodas() {
        return ResponseEntity.ok(geoService.obtenerTodas());
    }

    @GetMapping("/cercanos")
    @Operation(summary = "Buscar cercanas", description = "Busca alertas dentro de un radio en KM desde un punto dado")
    public ResponseEntity<List<UbicacionAlerta>> buscarCercanas(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radio) {
        return ResponseEntity.ok(geoService.buscarCercanas(lat, lon, radio));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ubicación", description = "Borra un registro de ubicación mediante su ID")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (geoService.eliminarUbicacion(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}