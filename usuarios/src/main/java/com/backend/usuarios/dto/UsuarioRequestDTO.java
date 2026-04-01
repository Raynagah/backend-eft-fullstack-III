package com.backend.usuarios.dto;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 50)
        String nombre,

        @NotNull
        @Min(18)
        @Max(100)
        Integer edad,

        @NotBlank(message = "El género es obligatorio")
        String genero,

        @Email
        @NotBlank
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        String fotoUrl,

        String ocupacion,

        String direccion
) {}