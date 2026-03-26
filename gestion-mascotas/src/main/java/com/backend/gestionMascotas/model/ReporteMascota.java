package com.backend.gestionMascotas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity // Indica que esta clase es una entidad JPA que se mapeará a una tabla
@Table(name = "reportes_mascotas") // Nombre de la tabla en tu base de datos
@Data // Anotación de Lombok que genera Getters, Setters, toString, etc.
@NoArgsConstructor // Lombok: Genera un constructor vacío (requerido por JPA)
@AllArgsConstructor // Lombok: Genera un constructor con todos los argumentos
@Builder // Lombok: Permite crear objetos usando el patrón Builder (muy útil junto al Factory Method)
public class ReporteMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- TIPO DE REPORTE ---
    @Column(name = "tipo_reporte", nullable = false)
    private String tipoReporte; // Ej: "PERDIDA" o "ENCONTRADA" (Aquí actuará tu Factory Method)

    // --- CARACTERÍSTICAS FÍSICAS  ---
    @Column(nullable = false)
    private String especie; // Ej: Perro, Gato

    private String raza;

    @Column(nullable = false)
    private String color;

    private String tamano; // Ej: Pequeño, Mediano, Grande

    // --- FOTOGRAFÍAS  ---
    @Column(name = "fotografia_url")
    private String fotografiaUrl; // Aquí guardaremos el enlace de Cloudinary o S3, no la imagen física

    // --- UBICACIÓN GEOGRÁFICA  ---
    // Aunque tienes un microservicio de geolocalización, es buena práctica guardar
    // las coordenadas de origen en el reporte principal
    private Double latitud;
    private Double longitud;

    // --- DATOS DE CONTACTO  ---
    @Column(name = "nombre_contacto", nullable = false)
    private String nombreContacto;

    @Column(name = "telefono_contacto", nullable = false)
    private String telefonoContacto;

    @Column(name = "email_contacto")
    private String emailContacto;

    // --- METADATOS ---
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    // JPA ejecutará este metodo justo antes de insertar el registro en la base de datos
    @PrePersist
    protected void onCreate() {
        this.fechaReporte = LocalDateTime.now();
    }
}