package com.backend.bff.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String telefono;
    private String email;
}