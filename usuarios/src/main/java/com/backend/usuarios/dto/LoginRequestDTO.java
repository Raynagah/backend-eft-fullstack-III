package com.backend.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        //Dto para login, solo correo y contraseña, ambos obligatorios
        @NotBlank
        String correo,

        @NotBlank
        String password
) {}