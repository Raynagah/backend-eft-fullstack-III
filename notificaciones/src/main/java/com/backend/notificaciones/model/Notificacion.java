package com.backend.notificaciones.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporteId;
    private Long usuarioId;      // Para la búsqueda técnica
    private String emailUsuario; // Para mostrar en el GET
    private Double porcentajeSimilitud;
    private String titulo;
    private String mensaje;
    private String fotografiaUrl;
    private LocalDateTime fechaCreacion;
    private boolean leido = false;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}