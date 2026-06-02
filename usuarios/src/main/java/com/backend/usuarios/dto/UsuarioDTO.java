package com.backend.usuarios.dto;

public record UsuarioDTO(
        // DTO para exponer datos del usuario sin incluir la contraseña ni otros campos sensibles
        Long id,
        String nombre,
        String telefono,
        String correo,
        Integer edad,
        String genero,
        String direccion,
        String ocupacion,
        String fotoUrl,
        String tipoUsuario
) {}