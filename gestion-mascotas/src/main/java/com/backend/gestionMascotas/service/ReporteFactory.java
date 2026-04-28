package com.backend.gestionMascotas.service;

import org.springframework.stereotype.Component;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;

@Component
public class ReporteFactory {

    public ReporteMascota crearReporte(ReporteRequestDTO dto) {

        // Preparamos el builder con los datos comunes del DTO
        ReporteMascota.ReporteMascotaBuilder builder = ReporteMascota.builder()
                .usuarioId(dto.usuarioId())
                .especie(dto.especie())
                .raza(dto.raza())
                .color(dto.color())
                .tamano(dto.tamano())
                .nombreContacto(dto.nombreContacto())
                .telefonoContacto(dto.telefonoContacto())
                .emailContacto(dto.emailContacto())
                .fotografiaUrl(dto.fotografiaUrl())
                .latitud(dto.latitud())
                .longitud(dto.longitud());

        //lógica de negocio del Factory
        String tipo = dto.tipoReporte().toUpperCase();

        if ("PERDIDA".equals(tipo) || "ENCONTRADA".equals(tipo)) {
            return builder.tipoReporte(tipo).build();
        } else {
            // Esta excepción será capturada en el GlobalExceptionHandler
            throw new IllegalArgumentException("El tipo de reporte debe ser 'PERDIDA' o 'ENCONTRADA'");
        }
    }
}