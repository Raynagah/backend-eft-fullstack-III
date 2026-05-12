package com.backend.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String telefono;

    // Usamos esta anotación para que Java entienda que en el JSON viene como 'correo'
    @JsonProperty("correo")
    private String email;

    private Integer edad;
    private String genero;
    private String direccion;
    private String ocupacion;
    private String fotoUrl;
}