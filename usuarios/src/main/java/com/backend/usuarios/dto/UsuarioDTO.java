package com.backend.usuarios.dto;

public record UsuarioDTO(
        Long id,
        String nombre,
        String telefono,
        String email // Lo llamamos email para que coincida con lo que espera el BFF/Frontend
) {}