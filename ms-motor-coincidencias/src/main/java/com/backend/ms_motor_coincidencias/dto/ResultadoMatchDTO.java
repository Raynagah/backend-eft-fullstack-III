package com.backend.ms_motor_coincidencias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultadoMatchDTO {

    // 1. Variables con los nombres exactos que envía ms-mascotas
    // así OpenFeign los mapeará automáticamente sin fallar.
    private Long id;
    private String nombre;

    private String descripcionMatch;
    private String especie;
    private String raza;
    private String color;
    private String tamano;
    private String tipoReporte;
    private String fotografiaUrl;

    @JsonProperty("emailContacto")
    private String emailContacto;

    private Double porcentajeSimilitud;

    // Cuando este microservicio responda al BFF, Jackson ejecutará
    // estos métodos y creará las llaves que el BFF está esperando.

    @JsonProperty("mascotaId")
    public Long getMascotaId() {
        return this.id;
    }

    @JsonProperty("nombreMascota")
    public String getNombreMascota() {
        return this.nombre;
    }
}