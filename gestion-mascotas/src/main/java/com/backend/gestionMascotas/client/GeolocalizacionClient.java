package com.backend.gestionMascotas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-geolocalizacion")
public interface GeolocalizacionClient {

    @PostMapping("/api/geolocalizacion")
    void registrarUbicacion(@RequestBody Object ubicacion);
}
