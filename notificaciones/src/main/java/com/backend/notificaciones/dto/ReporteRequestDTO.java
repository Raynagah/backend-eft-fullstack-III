package com.backend.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Constructor vacío (Vital para Jackson/Feign)
@AllArgsConstructor // Constructor con todos los campos
@Builder
@Schema(description = "Objeto de transferencia para los datos del reporte de mascotas")
public class ReporteRequestDTO {

        @NotNull(message = "El ID de usuario es obligatorio")
        private Long usuarioId;

        @Schema(description = "Define si la mascota fue perdida o encontrada", example = "PERDIDA")
        private String tipoReporte;

        @NotBlank(message = "La especie es obligatoria")
        private String especie;

        @NotBlank(message = "La raza es obligatoria")
        private String raza;

        @NotBlank(message = "El color es obligatorio")
        private String color;

        private String tamano;

        @NotBlank(message = "El nombre es obligatorio")
        private String nombreContacto;

        @NotBlank(message = "El teléfono es obligatorio")
        private String telefonoContacto;

        @NotBlank(message = "El correo es obligatorio")
        private String emailContacto;

        private String fotografiaUrl;

        @NotNull
        private Double latitud;

        @NotNull
        private Double longitud;
}