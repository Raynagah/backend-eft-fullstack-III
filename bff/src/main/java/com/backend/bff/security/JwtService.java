package com.backend.bff.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtService {

    // ⚠️ IMPORTANTE: Esta clave debe ser EXACTAMENTE la misma
    // que usas en el microservicio de Usuarios para generar el token.
    @Value("${jwt.secret:tu_clave_secreta_super_segura_y_larga_de_al_menos_32_caracteres}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token) // Si el token es inválido o expiró, esto lanza una excepción automáticamente
                .getBody();

        // Asumimos que al generar el token en el ms-usuarios, guardaste el ID del usuario en un claim llamado "userId".
        // Si lo guardaste en el "subject", sería: return Long.parseLong(claims.getSubject());
        return claims.get("userId", Long.class);
    }

    public String extractCorreo(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Generalmente el correo se guarda en el Subject
    }
}