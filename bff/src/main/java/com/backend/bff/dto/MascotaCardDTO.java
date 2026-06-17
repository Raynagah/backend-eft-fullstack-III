package com.backend.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MascotaCardDTO {
    private Long id;

    private String nombre;


    private String titulo;


    private String resumen;


    private String estado;

    private String tipoReporte;
    private String fotografiaUrl;
    private String fechaReporte;
}