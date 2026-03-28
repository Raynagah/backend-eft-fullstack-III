package com.backend.gestionMascotas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// El nombre debe ser EXACTAMENTE el que aparece en Eureka
@FeignClient(name = "ms-geolocalizacion")
public interface GeolocalizacionClient {

    @PostMapping("/api/geolocalizacion")
    void registrarUbicacion(@RequestBody Object ubicacion);
    // Usamos Object o un DTO simple para enviar los datos
}