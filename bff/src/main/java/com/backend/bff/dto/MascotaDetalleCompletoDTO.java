package com.backend.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MascotaDetalleCompletoDTO {

    // Datos base (ms-mascotas)
    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private String color;
    private String descripcion;
    private String estadoSaga;

    // Datos de contacto (ms-usuarios)
    private String contactoNombre;
    private String contactoTelefono;

    // Datos de ubicación aplanados (ms-geolocalizacion)
    private Double latitud;
    private Double longitud;

    // Posibles coincidencias (ms-motor-coincidencias)
    private List<CoincidenciaDTO> posiblesCoincidencias;
}