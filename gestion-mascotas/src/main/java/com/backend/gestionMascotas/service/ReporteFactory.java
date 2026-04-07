package com.backend.gestionMascotas.service;


import com.backend.gestionMascotas.model.ReporteMascota;
import org.springframework.stereotype.Component;

@Component 
public class ReporteFactory {

   
    public ReporteMascota crearReporte(String tipoReporte, String especie, String raza, String color, String tamano, String nombre, String telefono, String email, String fotoUrl, Double lat, Double lng) {

        
        ReporteMascota.ReporteMascotaBuilder builder = ReporteMascota.builder()
                .especie(especie)
                .raza(raza)
                .color(color)
                .tamano(tamano)
                .nombreContacto(nombre)
                .telefonoContacto(telefono)
                .emailContacto(email)
                .fotografiaUrl(fotoUrl)
                .latitud(lat)
                .longitud(lng);

       
        if ("PERDIDA".equalsIgnoreCase(tipoReporte)) {
            
            return builder.tipoReporte("PERDIDA").build();

        } else if ("ENCONTRADA".equalsIgnoreCase(tipoReporte)) {
            
            return builder.tipoReporte("ENCONTRADA").build();

        } else {
            throw new IllegalArgumentException("Error: El tipo de reporte debe ser 'PERDIDA' o 'ENCONTRADA'");
        }
    }
}
