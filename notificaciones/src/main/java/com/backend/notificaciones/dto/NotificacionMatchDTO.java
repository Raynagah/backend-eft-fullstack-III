package com.backend.notificaciones.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO para procesar una coincidencia detectada")
public class NotificacionMatchDTO {
    // DTO para recibir datos del motor de IA sobre coincidencias detectadas, con validaciones y anotaciones de Swagger
    @NotNull
    private Long reporteId;
    
    @NotNull
    private String emailUsuario; // Para saber a quién notificar

    @Schema(description = "Porcentaje calculado por el motor", example = "85.5")
    @DecimalMin("0.0")
    private Double porcentajeSimilitud;

    private String nombreMascotaCandidata;
    private String fotografiaUrl;
    private String titulo;
    private String mensaje;
    private LocalDateTime fechaCreacion;
    private boolean leido = false;
}