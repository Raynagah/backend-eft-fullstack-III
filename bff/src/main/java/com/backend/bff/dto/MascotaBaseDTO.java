package com.backend.bff.dto;

import lombok.Data;

@Data
public class MascotaBaseDTO {

    private Long id;
    private Long usuarioId;
    private String sagaStatus;

    // Datos físicos y del reporte
    private String nombre;
    private String tipoReporte;
    private String especie;
    private String raza;
    private String color;
    private String tamano;
    private String fotografiaUrl;

    // Ubicación (reemplaza al antiguo ubicacionId)
    private Double latitud;
    private Double longitud;

    // Datos de contacto (el microservicio ya los tiene redundados)
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;

    // Fecha del evento
    private String fechaReporte; // Lo dejamos como String para evitar problemas de parseo rápido
}