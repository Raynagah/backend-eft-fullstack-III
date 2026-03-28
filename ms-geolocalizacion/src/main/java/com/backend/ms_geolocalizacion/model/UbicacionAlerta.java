package com.backend.ms_geolocalizacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ubicaciones_alertas") // Tabla distinta para no mezclar
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionAlerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporteId; // El ID que viene del ms-mascotas
    private Double latitud;
    private Double longitud;
    private String tipoAlerta; // "PERDIDA" o "ENCONTRADA"
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}