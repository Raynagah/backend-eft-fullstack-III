package com.backend.gestionMascotas.dto;

import java.time.LocalDateTime;

public record ReporteResponseDTO(
        Long id,
        Long usuarioId,
        String tipoReporte,
        String nombre,
        String especie,
        String raza,
        String color,
        String tamano,
        String nombreContacto,
        String telefonoContacto,
        String fotografiaUrl,
        Double latitud,
        Double longitud,
        LocalDateTime fechaReporte,
        String sagaStatus
        // Nota: Omitimos usuarioId por seguridad x.x'
) {}