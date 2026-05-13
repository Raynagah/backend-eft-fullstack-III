package com.backend.bff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String telefono;

    @JsonProperty("correo") // Mapea 'correo' del JSON a 'email' en Java
    private String email;

    // Permite recibir la contraseña (escritura) pero no enviarla (lectura)
    private String password;

    private Integer edad;
    private String genero;
    private String direccion;
    private String ocupacion;
    private String fotoUrl;
}