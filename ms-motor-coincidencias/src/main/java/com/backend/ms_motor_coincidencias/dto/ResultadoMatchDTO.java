package com.backend.ms_motor_coincidencias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoMatchDTO {
    @JsonProperty("id")
    private Long reporteId;
    private String especie;
    private String raza;
    private String color;
    private String tamano;
    private String tipoReporte;
    private String fotografiaUrl;
    @JsonProperty("emailContacto")
    private String emailContacto; 
    
    private Double porcentajeSimilitud;
}