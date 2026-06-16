package com.backend.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoincidenciaDTO {
    private Long mascotaId;
    private String nombreMascota;
    private Double porcentajeSimilitud; // Ejemplo: 0.85 para 85%
    private String descripcionMatch;    // Ejemplo: "Coincide en raza y color"
    private String tipoReporte; //
}