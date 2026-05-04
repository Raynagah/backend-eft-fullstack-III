package com.backend.bff.client;

import com.backend.bff.dto.UbicacionDTO; // Crearemos este DTO en el BFF
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-geolocalizacion", url = "${services.geolocalizacion.url}")
public interface GeolocalizacionClient {

    // Este es el que usaremos para el agregado o.O
    @GetMapping("/api/geolocalizacion/reporte/{id}")
    UbicacionDTO obtenerUbicacionPorId(@PathVariable("id") Long id);
}