package com.backend.usuarios.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    // DTO para la respuesta del login, incluye token JWT, sessionId y datos del usuario
    private String token;
    private String sessionId;
    private UsuarioDTO usuario; // Usamos el DTO que ya teníamos para no exponer la entidad completa
}