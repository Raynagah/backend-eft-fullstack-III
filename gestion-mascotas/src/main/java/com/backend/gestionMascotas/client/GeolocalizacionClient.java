package com.backend.gestionMascotas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-geolocalizacion")
public interface GeolocalizacionClient {

    @PostMapping("/api/geolocalizacion")
    void registrarUbicacion(@RequestBody Object ubicacion);

    @DeleteMapping("/api/geolocalizacion/{id}")
    void eliminarUbicacion(@PathVariable("id") Long id);
}
