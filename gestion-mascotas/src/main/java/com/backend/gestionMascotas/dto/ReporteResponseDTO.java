package com.backend.gestionMascotas.dto;

import java.time.LocalDateTime;
import java.io.Serializable;

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
        String emailContacto,
        String fotografiaUrl,
        Double latitud,
        Double longitud,
        LocalDateTime fechaReporte,
        String sagaStatus

) implements Serializable {}