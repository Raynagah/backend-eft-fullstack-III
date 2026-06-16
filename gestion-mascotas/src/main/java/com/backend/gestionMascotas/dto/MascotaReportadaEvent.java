package com.backend.gestionMascotas.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MascotaReportadaEvent {

    private Long mascotaId;
    private Long usuarioId;
    private String nombreMascota;
    private String tipoReporte;

    // Constructores
    public MascotaReportadaEvent() {}

    public MascotaReportadaEvent(Long mascotaId, Long usuarioId, String nombreMascota, String estado) {
        this.mascotaId = mascotaId;
        this.usuarioId = usuarioId;
        this.nombreMascota = nombreMascota;
        this.tipoReporte = estado;
    }
}