package com.backend.usuarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Apagamos CSRF para que los POST de admin y cliente no sean bloqueados
            .csrf(csrf -> csrf.disable())
            
            // 2. Quitamos el estado de sesión clásico
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. Permisos explícitos
            .authorizeHttpRequests(auth -> auth
                // Permitimos todo lo de Swagger
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // 🔓 PERMITIMOS TU RUTA DE ADMIN Y LA NORMAL PARA QUE PUEDAS PROBAR EN SWAGGER
                .requestMatchers("/internal/admin/**").permitAll() 
                .requestMatchers("/api/usuarios/**").permitAll() 
                
                .anyRequest().authenticated()
            );

        return http.build();
    }
}