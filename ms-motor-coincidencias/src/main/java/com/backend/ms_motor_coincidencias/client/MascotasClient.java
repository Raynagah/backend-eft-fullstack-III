package com.backend.ms_motor_coincidencias.client;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

// El nombre debe ser el que usaste en el application.yml de Mascotas
@FeignClient(name = "ms-gestion-mascotas")
public interface MascotasClient {

    // Traer una mascota específica por su ID
    @GetMapping("/api/mascotas/{id}")
    ResultadoMatchDTO obtenerMascotaPorId(@PathVariable("id") Long id);

    // Traer todas las mascotas (aquí podríamos optimizar trayendo solo las "ENCONTRADAS",
    // pero por ahora pedimos todas para el cálculo)
    @GetMapping("/api/mascotas")
    List<ResultadoMatchDTO> obtenerTodasLasMascotas();
}