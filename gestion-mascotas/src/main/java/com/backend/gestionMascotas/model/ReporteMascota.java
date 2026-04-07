package com.backend.gestionMascotas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity 
@Table(name = "reportes_mascotas")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class ReporteMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- TIPO DE REPORTE ---
    @Column(name = "tipo_reporte", nullable = false)
    private String tipoReporte;

    // --- CARACTERÍSTICAS FÍSICAS  ---
    @Column(nullable = false)
    private String especie;

    private String raza;

    @Column(nullable = false)
    private String color;

    private String tamano; 

    // --- FOTOGRAFÍAS  ---
    @Column(name = "fotografia_url")
    private String fotografiaUrl; 

    // --- UBICACIÓN GEOGRÁFICA  ---
    private Double latitud;
    private Double longitud;

    // --- DATOS DE CONTACTO  ---
    @Column(name = "nombre_contacto", nullable = false)
    private String nombreContacto;

    @Column(name = "telefono_contacto", nullable = false)
    private String telefonoContacto;

    @Column(name = "email_contacto")
    @JsonProperty("emailContacto") 
    private String emailContacto;

    // --- METADATOS ---
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    @PrePersist
    protected void onCreate() {
        this.fechaReporte = LocalDateTime.now();
    }
}
