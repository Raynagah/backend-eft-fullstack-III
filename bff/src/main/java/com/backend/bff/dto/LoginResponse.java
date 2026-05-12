package com.backend.bff.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String sessionId;
    private UsuarioDTO usuario;
}