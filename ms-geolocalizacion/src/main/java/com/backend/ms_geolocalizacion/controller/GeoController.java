package com.backend.ms_geolocalizacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.service.GeoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Función: GeoController (Controlador)
 * Título: Controlador de Geolocalización
 * Descripción: Expone los endpoints REST para la gestión de ubicaciones espaciales, permitiendo registrar, consultar, eliminar y buscar alertas de mascotas perdidas o encontradas por proximidad geográfica.
 */
@RestController
@RequestMapping("/api/geolocalizacion")
@RequiredArgsConstructor
@Tag(name = "Geolocalización", description = "Servicios para la gestión de ubicaciones de mascotas")
public class GeoController {

    private final GeoService geoService;

    /**
     * Función: registrar
     * Título: Registrar ubicación
     * Descripción: Guarda las coordenadas geográficas (latitud y longitud) asociadas a una alerta de mascota en la base de datos.
     *
     * @param ubicacion Objeto UbicacionAlerta que contiene los datos espaciales y de la alerta a registrar.
     * @return ResponseEntity con el objeto UbicacionAlerta recién creado y un código HTTP 201 (CREATED).
     */
    @PostMapping
    @Operation(summary = "Registrar ubicación", description = "Guarda las coordenadas de una mascota perdida o encontrada")
    public ResponseEntity<UbicacionAlerta> registrar(@RequestBody UbicacionAlerta ubicacion) {
        UbicacionAlerta nueva = geoService.registrarUbicacion(ubicacion);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    /**
     * Función: obtenerTodas
     * Título: Obtener todas las ubicaciones
     * Descripción: Recupera un listado completo de todas las ubicaciones de alertas registradas actualmente en el sistema.
     *
     * @return ResponseEntity con una lista de objetos UbicacionAlerta y un código HTTP 200 (OK).
     */
    @GetMapping
    @Operation(summary = "Obtener todas", description = "Retorna una lista de todas las ubicaciones registradas")
    public ResponseEntity<List<UbicacionAlerta>> obtenerTodas() {
        return ResponseEntity.ok(geoService.obtenerTodas());
    }

    /**
     * Función: buscarCercanas
     * Título: Buscar alertas cercanas
     * Descripción: Realiza una búsqueda espacial para encontrar todas las alertas que se encuentren dentro de un radio de distancia específico a partir de un punto coordenado de origen.
     *
     * @param lat Latitud de tipo Double del punto central de la búsqueda.
     * @param lon Longitud de tipo Double del punto central de la búsqueda.
     * @param radio Distancia máxima en kilómetros (Double) para buscar alertas alrededor del punto dado.
     * @return ResponseEntity con una lista de objetos UbicacionAlerta que cumplen con el criterio de proximidad y un código HTTP 200 (OK).
     */
    @GetMapping("/cercanos")
    @Operation(summary = "Buscar cercanas", description = "Busca alertas dentro de un radio en KM desde un punto dado")
    public ResponseEntity<List<UbicacionAlerta>> buscarCercanas(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radio) {
        return ResponseEntity.ok(geoService.buscarCercanas(lat, lon, radio));
    }

    /**
     * Función: eliminar
     * Título: Eliminar ubicación
     * Descripción: Elimina permanentemente un registro de ubicación de la base de datos utilizando su identificador único.
     *
     * @param id Identificador único de tipo Long de la ubicación a borrar.
     * @return ResponseEntity vacío (Void) con un código HTTP 204 (NO CONTENT).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ubicación", description = "Borra un registro de ubicación mediante su ID")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        geoService.eliminarUbicacion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Función: obtenerPorReporte
     * Título: Obtener ubicación por reporte
     * Descripción: Busca y devuelve la información de ubicación geográfica asociada específicamente al identificador de un reporte.
     *
     * @param reporteId Identificador único de tipo Long del reporte asociado a la ubicación buscada.
     * @return ResponseEntity con el objeto UbicacionAlerta correspondiente y un código HTTP 200 (OK).
     */
    @GetMapping("/{reporteId}")
    @Operation(summary = "Obtener ubicación por reporte")
    public ResponseEntity<UbicacionAlerta> obtenerPorReporte(@PathVariable Long reporteId) {
        return ResponseEntity.ok(geoService.obtenerPorReporteId(reporteId));
    }
}