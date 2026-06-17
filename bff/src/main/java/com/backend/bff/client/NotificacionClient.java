package com.backend.bff.client;

import com.backend.bff.dto.NotificacionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-notificaciones", url = "${services.notificaciones.url}")
public interface NotificacionClient {

    @PostMapping("/api/notificaciones/enviar")
    void enviarAlertaMascota(@RequestBody NotificacionRequestDTO request);

    @GetMapping("/api/notificaciones/usuario/{usuarioId}")
    List<NotificacionRequestDTO> obtenerPorUsuario(@PathVariable("usuarioId") Long usuarioId);

    @PutMapping("/api/notificaciones/{id}/leer")
    NotificacionRequestDTO marcarComoLeida(@PathVariable("id") Long id);

    @DeleteMapping("/api/notificaciones/{id}")
    void eliminarNotificacion(@PathVariable("id") Long id);
}