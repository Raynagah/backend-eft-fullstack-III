package com.backend.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank
        String correo,

        @NotBlank
        String password
) {}