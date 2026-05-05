package com.backend.bff.client;

import com.backend.bff.dto.MascotaBaseDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// Usamos el nombre que definimos en Eureka/Docker
@FeignClient(name = "ms-gestion-mascotas", url = "${services.mascotas.url}")
public interface MascotasClient {

    @GetMapping("/api/mascotas/{id}")
    MascotaBaseDTO obtenerPorId(@PathVariable("id") Long id);

    @GetMapping("/api/mascotas")
    List<MascotaBaseDTO> obtenerTodas();

    @PostMapping("/api/mascotas") // La ruta real en ms-gestion-mascotas
    Object crear(@RequestBody WebReporteRequestDTO reporte);
}