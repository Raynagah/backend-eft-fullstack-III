package com.backend.ms_motor_coincidencias.client;

// Cambia la ruta al paquete local que acabas de crear
import com.backend.ms_motor_coincidencias.dto.external.NotificacionMatchDTO; 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "ms-notificaciones")
public interface NotificacionesClient {
    @PostMapping("/api/notificaciones/procesar-match")
    void enviarNotificaciones(@RequestBody List<NotificacionMatchDTO> coincidencias);
}