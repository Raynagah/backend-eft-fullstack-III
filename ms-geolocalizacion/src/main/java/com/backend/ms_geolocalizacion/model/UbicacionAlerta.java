package com.backend.ms_geolocalizacion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ubicaciones_alertas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Modelo que representa la ubicación geográfica de una alerta de mascota")
public class UbicacionAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "ID del reporte asociado en el microservicio de mascotas", example = "101")
    private Long reporteId;

    private Double latitud;
    private Double longitud;

    @Schema(description = "Tipo de alerta", example = "PERDIDA")
    private String tipoAlerta;

    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}