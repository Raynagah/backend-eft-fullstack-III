package com.backend.ms_motor_coincidencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsMotorCoincidenciasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsMotorCoincidenciasApplication.class, args);
	}

}
