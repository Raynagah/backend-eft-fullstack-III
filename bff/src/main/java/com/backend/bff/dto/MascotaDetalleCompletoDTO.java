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

    // --- Datos de ms-mascotas (Alineados con el JSON real) ---
    private Long id;
    private String nombre;
    private String tipoReporte; // Ej: "PERDIDA" o "ENCONTRADA"
    private String especie;
    private String raza;
    private String color;
    private String tamano;
    private String fotografiaUrl;
    private String sagaStatus; // Cambiado de estadoSaga para matchear el JSON
    private String fechaReporte;

    // --- Datos de contacto enriquecidos (ms-usuarios o fallback de ms-mascotas) ---
    private String contactoNombre;
    private String contactoTelefono;
    private String contactoEmail;

    // --- Datos de ubicación (ms-geolocalizacion o ms-mascotas) ---
    private Double latitud;
    private Double longitud;

    // --- Datos de inteligencia (ms-motor-coincidencias) ---
    private List<CoincidenciaDTO> posiblesCoincidencias;
}