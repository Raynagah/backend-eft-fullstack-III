package com.backend.bff.dto;

import lombok.Data;

@Data
public class WebReporteRequestDTO {
    private String nombre;
    private String especie;
    private String raza;
    private String color;
    private Double latitud;
    private Double longitud;
    private Long usuarioId; // Por ahora lo dejamos, luego lo sacaremos del token!!!!!!!!!!!
}