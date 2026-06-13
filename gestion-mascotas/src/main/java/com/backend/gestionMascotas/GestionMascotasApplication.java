package com.backend.gestionMascotas;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class GestionMascotasApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionMascotasApplication.class, args);
	}

}
