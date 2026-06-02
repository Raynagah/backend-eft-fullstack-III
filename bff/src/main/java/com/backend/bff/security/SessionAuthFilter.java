package com.backend.bff.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component; // ➡️ NUEVO IMPORT
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.bff.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // ➡️ NUEVO IMPORT

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String sessionId = request.getHeader("X-Session-ID");

        if (authHeader == null || !authHeader.startsWith("Bearer ") || sessionId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 1. Extraemos los datos REALES del token
            Long userId = jwtService.extractUserId(token);
            String correo = jwtService.extractCorreo(token);
            String rol = jwtService.extractRol(token); // ➡️ NUEVA LÍNEA: Extraemos el rol del claim del JWT

            // 2. Validamos la sesión en el microservicio de Usuarios (Neon DB / Redis)
            if (!authService.esSesionValida(userId, sessionId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Sesion invalida o iniciada en otro dispositivo");
                return;
            }

            // 3. CONTEXTO DE SEGURIDAD REAL:
            // Si llegamos aquí, el token es válido y la sesión es correcta.
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // ➡️ NUEVA LÍNEA: Creamos la autoridad/rol mapeada con el prefijo "ROLE_" que exige Spring Security.
                // Convierte 'admin' en 'ROLE_ADMIN' y 'cliente' en 'ROLE_CLIENTE'
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase())
                );

                // Creamos el objeto de autenticación inyectando las autoridades reales
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        correo,       // El principal (identificador del usuario)
                        null,         // Las credenciales (no se necesitan, ya confiamos en el token)
                        authorities   // ➡️ MODIFICACIÓN: Pasamos los roles reales en lugar de una lista vacía
                );

                // Le agregamos los detalles de la petición web (IP, metadatos, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guardamos el usuario autenticado con su respectivo Rol en el contexto global de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            // Si parseClaimsJws falla (token modificado, firma inválida o expirado), cae aquí.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}