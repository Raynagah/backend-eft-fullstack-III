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
@Tag(name = "Notificaciones", description = "Operaciones de alertas y mensajería")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Procesar coincidencias", description = "Filtra por >= 85% y guarda la notificación.")
    @PostMapping("/procesar-match")
    public ResponseEntity<String> recibirCoincidencias(@Valid @RequestBody List<NotificacionMatchDTO> dtos) {
        notificacionService.procesarNotificaciones(dtos);
        return ResponseEntity.ok("Notificaciones procesadas y guardadas.");
    }

    @Operation(summary = "Obtener notificaciones de un usuario", description = "Retorna el historial de alertas por email.")
    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(@PathVariable String email) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(email));
    }
}