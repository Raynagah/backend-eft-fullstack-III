package com.backend.usuarios.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // 1. Leemos la clave secreta desde las propiedades de configuración
    // Si no la encuentra, usa un valor por defecto (útil para desarrollo)
    @Value("${jwt.secret:tu_clave_secreta_super_segura_y_larga_de_al_menos_32_caracteres}")
    private String secretKey;

    private final long EXPIRATION = 86400000;

    // 2. Método para convertir el String en la llave criptográfica real
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 3. Agregamos el userId a los parámetros para que el BFF pueda leerlo luego y asociar el token al usuario correcto
    public String generarToken(String correo, String sessionId, Long userId, String tipoUsuario) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("sessionId", sessionId)
                .claim("userId", userId)
                .claim("rol", tipoUsuario)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}