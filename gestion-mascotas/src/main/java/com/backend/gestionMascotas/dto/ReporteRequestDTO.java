package com.backend.gestionMascotas.dto;

public record ReporteRequestDTO(
        String tipoReporte,
        String especie,
        String raza,
        String color,
        String tamano,
        String nombreContacto,
        String telefonoContacto,
        String emailContacto,
        String fotografiaUrl,
        Double latitud,
        Double longitud
) {}
