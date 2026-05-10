package com.backend.bff.security;

import com.backend.bff.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Dejar pasar peticiones públicas (como el login)
        if (path.startsWith("/api/v1/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer los Headers
        String authHeader = request.getHeader("Authorization");
        String sessionId = request.getHeader("X-Session-ID");

        // 3. Verificaciones de existencia
        if (authHeader == null || !authHeader.startsWith("Bearer ") || sessionId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Falta el token de autorizacion o el Session-ID");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 4. AQUÍ decodificamos nuestro JWT real para obtener el ID del usuario.
            // Para mantener el hilo de nuestro ejemplo, simularemos que decodificamos el ID 101L.
            Long userIdExtraidoDelToken = 101L; // Reemplazar con: jwtService.extractUserId(token);

            // 5. REQUISITO: Validar Sesión Única
            if (!authService.esSesionValida(userIdExtraidoDelToken, sessionId)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Sesion invalida o iniciada en otro dispositivo");
                return;
            }

            // 6. Si el token es válido y la sesión es la correcta, configurar el contexto de seguridad.
            // (Aquí normalmente instanciamos un UsernamePasswordAuthenticationToken y lo guardamos en el SecurityContextHolder)

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido o expirado");
            return;
        }

        // 7. Si todoo está correcto, dejamos que la petición continúe hacia el Controlador
        filterChain.doFilter(request, response);
    }
}