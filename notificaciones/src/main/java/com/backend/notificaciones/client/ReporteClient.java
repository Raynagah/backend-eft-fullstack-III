package com.backend.notificaciones.client;

import com.backend.notificaciones.dto.ReporteRequestDTO; // Importa el DTO del otro MS
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El name debe coincidir con el nombre del MS de reportes en Eureka/Config
@FeignClient(name = "ms-gestion-mascotas")
public interface ReporteClient {

    @GetMapping("/api/mascotas/{id}")
    ReporteRequestDTO obtenerReportePorId(@PathVariable("id") Long id);
}