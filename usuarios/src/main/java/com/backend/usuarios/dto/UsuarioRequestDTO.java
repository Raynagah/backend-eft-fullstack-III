package com.backend.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 50)
        String nombre,

        @NotNull(message = "La edad es obligatoria y tiene extensión entre 18 y 100")
        @Min(18)
        @Max(100)
        Integer edad,

        @NotBlank(message = "El género es obligatorio")
        String genero,

        @Email
        @NotBlank(message = "El correo es obligatorio")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        String fotoUrl,

        String ocupacion,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion
) {}