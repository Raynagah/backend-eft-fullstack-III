package com.backend.ms_geolocalizacion.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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