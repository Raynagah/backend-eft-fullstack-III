package com.backend.gestionMascotas.service;

import org.springframework.stereotype.Component;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;

/**
 * Función: ReporteFactory (Clase Factory)
 * Título: Fábrica de Reportes de Mascotas
 * Descripción: Componente de Spring que implementa el patrón de creación Factory Method para encapsular y centralizar la lógica de instanciación de entidades ReporteMascota, aislando las validaciones de negocio iniciales del servicio principal.
 */
@Component
public class ReporteFactory {

    /**
     * Función: crearReporte
     * Título: Instanciar Reporte de Mascota
     * Descripción: Transfiere los datos desde el objeto de transferencia (ReporteRequestDTO) hacia un Builder de la entidad ReporteMascota. Aplica una regla de negocio estricta para asegurar que el tipo de reporte sea exclusivamente "PERDIDA" o "ENCONTRADA" antes de finalizar la construcción del objeto.
     *
     * @param dto Objeto ReporteRequestDTO que contiene los datos de la mascota ingresados a través del controlador.
     * @return El objeto de dominio ReporteMascota completamente instanciado, validado y listo para ser persistido.
     * @throws IllegalArgumentException Si el tipo de reporte especificado en el DTO no coincide con los valores permitidos ("PERDIDA" o "ENCONTRADA").
     */
    public ReporteMascota crearReporte(ReporteRequestDTO dto) {

        // Preparamos el builder con los datos comunes del DTO
        ReporteMascota.ReporteMascotaBuilder builder = ReporteMascota.builder()
                .usuarioId(dto.usuarioId())
                .nombre(dto.nombre())
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