package com.backend.bff.dto;

import lombok.Data;

@Data
public class MascotaBaseDTO {
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private String color;
    private String sagaStatus; // Importante para ver si el reporte fue exitoso!!!!!!!!
}