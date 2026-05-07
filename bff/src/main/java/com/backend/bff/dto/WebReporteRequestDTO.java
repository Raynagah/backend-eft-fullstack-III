package com.backend.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebReporteRequestDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El tipo de reporte (PERDIDA/ENCONTRADA) es obligatorio")
    private String tipoReporte;

    @NotBlank(message = "Debes especificar la especie (Perro, Gato, etc.)")
    private String especie;

    // Lo dejamos opcional como bien pensaste, sin @NotBlank
    private String raza;

    @NotBlank(message = "El color principal es obligatorio para la búsqueda")
    private String color;

    private String tamano; // Puede ser opcional

    private String fotografiaUrl;

    @NotNull(message = "La latitud es necesaria para ubicar a la mascota")
    private Double latitud;

    @NotNull(message = "La longitud es necesaria para ubicar a la mascota")
    private Double longitud;

    // Datos de contacto que el micro de mascotas espera recibir
    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Size(min = 2, max = 100)
    private String nombreContacto;

    @NotBlank(message = "El teléfono de contacto es vital para avisar al dueño")
    private String telefonoContacto;

    private String emailContacto;
}