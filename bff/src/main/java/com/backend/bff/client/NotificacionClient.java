package com.backend.bff.client;

import com.backend.bff.dto.NotificacionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-notificaciones", url = "${services.notificaciones.url}")
public interface NotificacionClient {

    @PostMapping("/api/notificaciones/enviar")
    void enviarAlertaMascota(@RequestBody NotificacionRequestDTO request);
}