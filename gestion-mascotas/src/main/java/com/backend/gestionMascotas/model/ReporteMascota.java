package com.backend.gestionMascotas.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Actualizado para coincidir con el DTO y ser obligatorio
    @Column(name = "usuario_id", nullable = false)
    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    // NUEVO: Campo para el Patrón Saga (Requisito Bloque 3)
    @Column(name = "saga_status", nullable = false)
    private String sagaStatus;

    @NotBlank(message = "El tipo de reporte no puede estar vacío")
    @Column(name = "tipo_reporte", nullable = false)
    private String tipoReporte;

    @NotBlank(message = "La especie es obligatoria")
    @Column(nullable = false)
    private String especie;

    @NotBlank(message = "La raza es obligatoria")
    @Column(nullable = false)
    private String raza;

    @NotBlank(message = "El color es obligatorio")
    @Column(nullable = false)
    private String color;

    private String tamano;

    @Column(name = "fotografia_url")
    private String fotografiaUrl;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Column(name = "nombre_contacto", nullable = false)
    private String nombreContacto;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Column(name = "telefono_contacto", nullable = false)
    private String telefonoContacto;

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email de contacto es obligatorio")
    @Column(name = "email_contacto", nullable = false)
    @JsonProperty("emailContacto")
    private String emailContacto;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    @PrePersist
    protected void onCreate() {
        this.fechaReporte = LocalDateTime.now();
        // Inicializamos el estado de la saga automáticamente al crear el registro
        if (this.sagaStatus == null) {
            this.sagaStatus = "PENDING";
        }
    }
}