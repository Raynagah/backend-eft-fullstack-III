package com.backend.bff.security;

import com.backend.bff.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService; // Inyectamos nuestro nuevo servicio

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

            // 2. Validamos la sesión en Redis/Base de datos
            if (!authService.esSesionValida(userId, sessionId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Sesion invalida o iniciada en otro dispositivo");
                return;
            }

            // 3. CONTEXTO DE SEGURIDAD REAL:
            // Si llegamos aquí, el token es válido y la sesión es correcta.
            // Le decimos a Spring Security "¡Este usuario está autenticado!"
            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Creamos el objeto de autenticación (Aquí podrías pasar los roles si los tuvieras en vez de un ArrayList vacío)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        correo, // El principal (identificador del usuario)
                        null,   // Las credenciales (ya no las necesitamos porque confiamos en el token)
                        new ArrayList<>() // Authorities/Roles (vacío por ahora)
                );

                // Le agregamos los detalles de la petición web (IP, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lo guardamos en el contexto global de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            // Si parseClaimsJws falla (token modificado o expirado), cae aquí.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}