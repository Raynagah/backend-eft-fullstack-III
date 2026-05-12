package com.backend.ms_motor_coincidencias.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "coincidencias_registradas")
@Data
public class Coincidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporteOriginalId;
    private Long reporteEncontradoId;
    private Double porcentajeSimilitud;
    private String emailNotificado;
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
