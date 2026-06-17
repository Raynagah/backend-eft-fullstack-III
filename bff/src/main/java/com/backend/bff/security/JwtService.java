package com.backend.bff.security;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // IMPORTANTE: Esta clave debe ser EXACTAMENTE la misma
    // que usamos en el microservicio de Usuarios para generar el token.
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

        return claims.get("userId", Long.class);
    }

    public String extractCorreo(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRol(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

    // Extraemos el claim "rol" que guardamos en el microservicio de usuarios
    return claims.get("rol", String.class); 
    }
}