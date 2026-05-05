package com.backend.ms_motor_coincidencias.dto.external;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificacionMatchDTO {
    private Long reporteId;
    private String emailUsuario; 
    private Double porcentajeSimilitud;
    private String nombreMascotaCandidata;
    private String fotografiaUrl;
    private String titulo;  
    private String mensaje; 
    private LocalDateTime fechaCreacion;
    private boolean leido = false;
}