package com.backend.usuarios.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    // Este bean dummy es solo para asegurar que Spring Security se active, ya que no estamos usando la configuración tradicional de WebSecurityConfigurerAdapter
    @Bean
    public String dummyBean() {
        return "security-enabled";
    }
}