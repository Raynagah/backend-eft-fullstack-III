package com.backend.bff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioAdminDTO {
    private Long id;
    private String nombre;
    private String correo;
    private String telefono;
    private String tipoUsuario; // "admin" o "cliente"
    private String ocupacion;   // Asumiendo que ms-usuarios guarda esto
    private Integer cantidadReportes; // ¡El campo estrella!
}