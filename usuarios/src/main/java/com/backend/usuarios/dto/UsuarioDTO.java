package com.backend.usuarios.dto;

public record UsuarioDTO(
        Long id,
        String nombre,
        String telefono,
        String correo
) {}