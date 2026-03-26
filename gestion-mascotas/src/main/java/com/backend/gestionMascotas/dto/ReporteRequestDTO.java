package com.backend.gestionMascotas.dto;

// Un "record" en Java es perfecto para los DTOs porque crea los getters y setters
// de forma automática y es inmutable (más seguro).
public record ReporteRequestDTO(
        String tipoReporte,
        String especie,
        String raza,
        String color,
        String tamano,
        String nombreContacto,
        String telefonoContacto,
        String fotografiaUrl,
        Double latitud,
        Double longitud
) {}