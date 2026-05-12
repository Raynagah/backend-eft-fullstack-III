package com.backend.usuarios.dto;

public record UsuarioDTO(
        Long id,
        String nombre,
        String telefono,
        String correo,
        Integer edad,
        String genero,
        String direccion,
        String ocupacion,
        String fotoUrl
) {}