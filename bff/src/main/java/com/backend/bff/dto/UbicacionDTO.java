package com.backend.bff.dto;

import lombok.Data;

@Data
public class UbicacionDTO {
    private Long id;
    private Double latitud;
    private Double longitud;
    private String direccion;
}