package com.backend.ms_geolocalizacion.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        servers = {@Server(url = "/", description = "Servidor local")},
        info = @Info(
                title = "MS Geolocalización API",
                version = "1.0",
                description = "Microservicio de geolocalización"
        )
)
public class SwaggerConfig {
}