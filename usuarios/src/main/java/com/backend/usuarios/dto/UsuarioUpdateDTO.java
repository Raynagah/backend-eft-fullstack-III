package com.backend.usuarios.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        // DTO para actualizar datos del usuario, similar al request pero sin correo ni password
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 50)
        String nombre,

        @NotNull(message = "La edad es obligatoria")
        @Min(18) @Max(100)
        Integer edad,

        @NotBlank(message = "El género es obligatorio")
        String genero,

        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,

        String fotoUrl,
        String ocupacion,
        String direccion,

        @NotBlank(message = "El tipo de usuario es obligatorio")
        @Pattern(regexp = "^(admin|cliente)$", message = "El rol solo puede ser 'admin' o 'cliente'")
        String tipoUsuario
) {}