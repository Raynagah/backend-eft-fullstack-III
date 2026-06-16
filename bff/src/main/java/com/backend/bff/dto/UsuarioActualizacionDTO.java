package com.backend.bff.dto;

import lombok.Data;

@Data
public class UsuarioActualizacionDTO {
    private String nombre;
    private Integer edad;
    private String genero;
    private String telefono;
    private String fotoUrl;
    private String ocupacion;
    private String direccion;
    private String tipoUsuario;
}