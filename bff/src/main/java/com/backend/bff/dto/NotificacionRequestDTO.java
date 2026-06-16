package com.backend.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionRequestDTO {
    private Long id;
    private Long reporteId;
    private Long usuarioId;
    private String emailUsuario;
    private Double porcentajeSimilitud;
    private String titulo;
    private String mensaje;
    private String fotografiaUrl;
    private LocalDateTime fechaCreacion;
    private boolean leido;
}