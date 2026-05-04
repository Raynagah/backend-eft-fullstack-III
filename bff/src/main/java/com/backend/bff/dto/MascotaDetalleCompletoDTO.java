package com.backend.bff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MascotaDetalleCompletoDTO {
    // Datos de ms-gestion-mascotas
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private String color;
    private String estadoSaga;

    // Datos de ms-geolocalizacion (Agregados)
    private Double latitud;
    private Double longitud;
}