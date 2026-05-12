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

    // Título principal que armamos en el Service (Ej: "PERDIDA: Perro Labrador")
    private String titulo;

    // Subtítulo descriptivo (Ej: "Color: Negro - Tamaño: Grande")
    private String resumen;

    // El estado del patrón Saga (PENDING, COMPLETED, etc.)
    private String estado;

    private String tipoReporte;
    private String fotografiaUrl;
}