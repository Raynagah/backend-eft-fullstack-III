package com.backend.bff.client;

import com.backend.bff.dto.CoincidenciaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-motor-coincidencias", url = "${services.coincidencias.url}")
public interface CoincidenciasClient {

    @GetMapping("/api/coincidencias/buscar/{id}")
    List<CoincidenciaDTO> obtenerCoincidenciasPorMascota(@PathVariable("id") Long id);

}