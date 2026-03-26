package com.backend.gestionMascotas.service;


import com.backend.gestionMascotas.model.ReporteMascota;
import org.springframework.stereotype.Component;

@Component // Le dice a Spring que administre esta clase para poder inyectarla
public class ReporteFactory {

    // Este es nuestro Factory Method
    public ReporteMascota crearReporte(String tipoReporte, String especie, String raza, String color, String tamano, String nombre, String telefono, String fotoUrl, Double lat, Double lng) {

        // Usamos el patrón Builder que nos regaló Lombok en la entidad
        ReporteMascota.ReporteMascotaBuilder builder = ReporteMascota.builder()
                .especie(especie)
                .raza(raza)
                .color(color)
                .tamano(tamano)
                .nombreContacto(nombre)
                .telefonoContacto(telefono)
                .fotografiaUrl(fotoUrl)
                .latitud(lat)
                .longitud(lng);

        // Lógica de decisión de la fábrica basada en el tipo
        if ("PERDIDA".equalsIgnoreCase(tipoReporte)) {
            // Aquí en el futuro podrías inicializar atributos específicos para perdidas
            return builder.tipoReporte("PERDIDA").build();

        } else if ("ENCONTRADA".equalsIgnoreCase(tipoReporte)) {
            // Aquí en el futuro podrías inicializar atributos específicos para encontradas
            return builder.tipoReporte("ENCONTRADA").build();

        } else {
            throw new IllegalArgumentException("Error: El tipo de reporte debe ser 'PERDIDA' o 'ENCONTRADA'");
        }
    }
}
