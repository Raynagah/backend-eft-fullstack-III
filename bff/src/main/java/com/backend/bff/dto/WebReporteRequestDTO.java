package com.backend.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WebReporteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "Debes especificar la especie (Perro, Gato, etc.)")
    private String especie;

    private String raza; // Este puede ser opcional dependiendo de la lógica que vayamos a implementar, no me decidí sobre si debía quedar obligatoria o no xd

    @NotNull(message = "La latitud es necesaria para ubicar a la mascota")
    private Double latitud;

    @NotNull(message = "La longitud es necesaria para ubicar a la mascota")
    private Double longitud;

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;
}