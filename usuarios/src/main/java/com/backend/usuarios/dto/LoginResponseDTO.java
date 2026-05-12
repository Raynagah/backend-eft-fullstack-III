package com.backend.usuarios.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String sessionId;
    private UsuarioDTO usuario; // Usamos el DTO que ya teníamos para no exponer la entidad completa
}