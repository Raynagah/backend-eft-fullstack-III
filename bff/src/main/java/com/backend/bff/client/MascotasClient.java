package com.backend.bff.client;

import com.backend.bff.dto.MascotaBaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

// Usamos el nombre que definimos en Eureka/Docker
@FeignClient(name = "ms-gestion-mascotas", url = "${services.mascotas.url}")
public interface MascotasClient {

    @GetMapping("/api/mascotas/{id}")
    MascotaBaseDTO obtenerPorId(@PathVariable("id") Long id);

    @GetMapping("/api/mascotas")
    List<MascotaBaseDTO> obtenerTodas();
}