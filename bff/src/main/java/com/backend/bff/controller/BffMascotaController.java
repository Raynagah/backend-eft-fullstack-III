package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import com.backend.bff.service.BffMascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/web/mascotas")
@RequiredArgsConstructor
public class BffMascotaController {

    private final BffMascotaService bffService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<MascotaCardDTO>> obtenerDashboard() {
        return ResponseEntity.ok(bffService.obtenerDashboard());
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<MascotaDetalleCompletoDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(bffService.obtenerDetalleMascota(id));
    }

    @PostMapping("/reportar")
    public ResponseEntity<?> crearReporte(@Valid @RequestBody WebReporteRequestDTO webDto) {
        // Si llega aquí, es porque los datos ya son válidos
        var response = bffService.crearNuevoReporte(webDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}