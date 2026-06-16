package com.backend.notificaciones.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MascotaReportadaEvent {
    // Getters, Setters y constructor(es)
    private Long mascotaId;
    private Long usuarioId;
    private String nombreMascota;
    private String tipoReporte;

    public MascotaReportadaEvent() {}

    public String getEstado() { return tipoReporte; }

    public void setEstado(String estado) { this.tipoReporte = estado; }

    @Override
    public String toString() {
        return "MascotaReportadaEvent{mascotaId=" + mascotaId + ", usuarioId=" + usuarioId +
                ", nombreMascota='" + nombreMascota + "', estado='" + tipoReporte + "'}";
    }
}