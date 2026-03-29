package com.backend.ms_motor_coincidencias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoMatchDTO {
    private Long reporteId;
    private String especie;
    private String raza;
    private String color;
    private String tamano;
    private String tipoReporte;
    private String fotografiaUrl;

    // ¡El dato estrella!
    private Double porcentajeSimilitud;
}