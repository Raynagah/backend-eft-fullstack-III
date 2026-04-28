package com.backend.gestionMascotas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Objeto de transferencia para la creación de reportes de mascotas")
public record ReporteRequestDTO(
        //Campo para evitar reportes anónimos
        @Schema(description = "Identificador único del usuario que realiza el reporte", example = "125")
        @NotNull(message = "El ID de usuario es obligatorio para vincular el reporte")
        Long usuarioId,

        @Schema(description = "Define si la mascota fue perdida o encontrada", example = "PERDIDA")
        String tipoReporte,

        @Schema(description = "Especie del animal", example = "Perro")
        @NotBlank(message = "La especie es obligatoria")
        @Size(min = 3, max = 50)
        String especie,

        @Schema(description = "Raza de la mascota", example = "Golden Retriever")
        @NotBlank(message = "La raza es obligatoria")
        @Size(min = 3, max = 50)
        String raza,

        @Schema(description = "Color predominante", example = "Dorado")
        @NotBlank(message = "El color es obligatorio")
        @Size(min = 3, max = 50)
        String color,

        @Schema(description = "Tamaño aproximado", example = "Grande")
        String tamano,

        @Schema(description = "Nombre de la persona que reporta", example = "Juan Pérez")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 50)
        String nombreContacto,

        @Schema(description = "Número telefónico de contacto", example = "+56912345678")
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 3, max = 50)
        String telefonoContacto,

        @Schema(description = "Correo electrónico para contacto", example = "juan.perez@email.com")
        @NotBlank(message = "El correo es obligatorio")
        @Size(min = 3, max = 50)
        String emailContacto,

        @Schema(description = "URL de la imagen almacenada en la nube", example = "https://mi-storage.com/foto-perro.jpg")
        String fotografiaUrl,

        @Schema(description = "Latitud geográfica", example = "-34.6037")
        @NotNull
        Double latitud,

        @Schema(description = "Longitud geográfica", example = "-58.3816")
        @NotNull
        Double longitud
) {}