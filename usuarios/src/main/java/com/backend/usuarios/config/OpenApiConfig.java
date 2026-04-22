package com.backend.usuarios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {@Server(url = "/", description = "Servidor local")},
        info = @Info(
                title = "MS Usuarios API",
                version = "1.0",
                description = "Microservicio de usuarios con login JWT"
        )
)
public class OpenApiConfig {
}