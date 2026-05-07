package com.backend.notificaciones.controller;

import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de alertas y estados de lectura")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Procesar coincidencias", description = "Recibe matches del motor de IA.")
    @PostMapping("/procesar-match")
    public ResponseEntity<String> recibirCoincidencias(@Valid @RequestBody List<NotificacionMatchDTO> dtos) {
        notificacionService.procesarNotificaciones(dtos);
        return ResponseEntity.ok("Notificaciones procesadas exitosamente.");
    }

    @Operation(summary = "Marcar notificación como leída", description = "Cambia el estado de la variable 'leido' a true.")
    @PatchMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    @Operation(summary = "Obtener por ID de usuario", description = "Búsqueda técnica por ID.")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioId));
    }

    @Operation(summary = "Eliminar notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener todas las notificaciones")
    @GetMapping("/todas")
    public ResponseEntity<List<Notificacion>> obtenerTodas() {
        return ResponseEntity.ok(notificacionService.obtenerTodas());
    }
}