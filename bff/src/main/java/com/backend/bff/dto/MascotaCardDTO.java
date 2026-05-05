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
    private String resumen; // Ejemplo: "Perro - Labrador - Balú prime"
    private String estado;  // El estado de la Saga (COMPLETED, PENDING, etc.)
}