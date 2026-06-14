package com.backend.usuarios.dto;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO implements Serializable{
    // DTO para la respuesta del login, incluye token JWT, sessionId y datos del usuario
    private String token;
    private String sessionId;
    private UsuarioDTO usuario; // Usamos el DTO que ya teníamos para no exponer la entidad completa
}