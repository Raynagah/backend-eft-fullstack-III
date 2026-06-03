package com.backend.bff.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.bff.security.SessionAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SessionAuthFilter sessionAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. FUNDAMENTAL: Permitir peticiones OPTIONS (CORS preflight) para que Vue no reciba 403
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. RUTAS DE SWAGGER / OPENAPI (Con prefijos explícitos para Spring Boot 3)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api/v1/web/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/v1/web/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 1. PERMITIR ACTUATOR SIN AUTENTICACIÓN:
                        .requestMatchers("/actuator/health").permitAll()

                        // 3. Rutas públicas
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/registro").permitAll()
                        .requestMatchers("/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/web/mascotas/**").permitAll()
                        .requestMatchers("/api/v1/web/usuarios/registro").permitAll()

                        // 4. 🔒 RUTAS EXCLUSIVAS DE ADMINISTRADOR
                        // Cualquier ruta que empiece con /admin/ requiere obligatoriamente el rol ADMIN
                        .requestMatchers("/api/v1/web/admin/**").hasRole("ADMIN")

                        // 5. Rutas protegidas genéricas (Para Clientes y Admins)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Se agregó PATCH y se asegura de que OPTIONS esté permitido
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}