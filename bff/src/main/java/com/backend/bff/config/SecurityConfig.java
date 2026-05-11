package com.backend.bff.config;

import com.backend.bff.security.SessionAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SessionAuthFilter sessionAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Habilitamos CORS directamente en Spring Security
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Desactivamos CSRF porque usaremos tokens (JWT)
                .csrf(csrf -> csrf.disable())
                // 3. Desactivamos el manejo de sesiones clásico de Spring (JSESSIONID)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4. Configuramos las reglas de las rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login").permitAll() // Público
                        // Si tienes un endpoint de logout, también debes permitirlo o manejarlo
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .anyRequest().authenticated() // Todo lo demás requiere pasar por el filtro
                )
                // 5. Añadimos nuestro filtro personalizado ANTES del filtro estándar de autenticación
                .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean encargado de definir las reglas CORS para Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Tu frontend Vue
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicamos estas reglas a todas las rutas
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}