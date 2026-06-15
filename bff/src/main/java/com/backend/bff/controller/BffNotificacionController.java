package com.backend.bff.controller;

import com.backend.bff.service.BffNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/web/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BffNotificacionController {

    // Inyectamos el servicio, no el Feign Client
    private final BffNotificacionService bffNotificacionService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerMisNotificaciones(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(bffNotificacionService.obtenerMisNotificaciones(usuarioId));
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(bffNotificacionService.marcarComoLeida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        bffNotificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}