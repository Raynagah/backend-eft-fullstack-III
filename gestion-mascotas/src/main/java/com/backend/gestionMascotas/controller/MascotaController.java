package com.backend.gestionMascotas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.dto.ReporteResponseDTO; // Importamos solo los DTOs
import com.backend.gestionMascotas.service.MascotaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Función: MascotaController (Controlador)
 * Título: Controlador de Gestión de Mascotas
 * Descripción: Expone los endpoints REST para la gestión integral de reportes de mascotas (perdidas o encontradas), manejando el registro (iniciando el flujo de una Saga), consulta, filtrado y eliminación, así como las transacciones compensatorias.
 */
@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
@Tag(name = "Reportes de Mascotas", description = "Operaciones para gestionar el registro, búsqueda y eliminación de reportes")
public class MascotaController {

    private final MascotaService mascotaService;

    /**
     * Función: crearReporte
     * Título: Crear nuevo reporte (Inicio de Saga)
     * Descripción: Recibe y valida los datos para registrar un nuevo reporte de mascota. Actúa como el punto de inicio para una transacción distribuida (Saga), guardando la información localmente y orquestando llamadas subsecuentes.
     *
     * @param dto Objeto ReporteRequestDTO con los datos validados del reporte a crear.
     * @return ResponseEntity con el objeto ReporteResponseDTO creado y un código HTTP 201 (CREATED).
     */
    @Operation(summary = "Registrar un nuevo reporte (Inicio de Saga)")
    @PostMapping
    public ResponseEntity<ReporteResponseDTO> crearReporte(@Valid @RequestBody ReporteRequestDTO dto) {
        ReporteResponseDTO nuevoReporte = mascotaService.registrarReporte(dto);
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED);
    }

    /**
     * Función: eliminarReporte
     * Título: Eliminar reporte por ID
     * Descripción: Borra un reporte de mascota existente basándose en su identificador único. Además de la eliminación local, solicita la eliminación de la geolocalización asociada en los servicios correspondientes.
     *
     * @param id Identificador único de tipo Long del reporte a eliminar.
     * @return ResponseEntity vacío (Void) con un código HTTP 204 (NO CONTENT) si la operación fue exitosa.
     */
    @Operation(summary = "Eliminar reporte por ID", description = "Elimina el reporte del sistema local y solicita la eliminación de su geolocalización.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reporte eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reporte no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Long id) {
        mascotaService.eliminarReporte(id);
        return ResponseEntity.noContent().build(); // Retorna 204
    }

    /**
     * Función: compensarReporte
     * Título: Compensar transacción de reporte
     * Descripción: Endpoint de uso interno para el patrón Saga. Permite revertir (compensar) el estado de un reporte previamente registrado en caso de que alguna transacción subsecuente en otro microservicio haya fallado.
     *
     * @param id Identificador único de tipo Long del reporte cuya transacción debe ser compensada.
     * @return ResponseEntity vacío (Void) con un código HTTP 200 (OK).
     */
    @Operation(summary = "Compensar transacción de reporte")
    @PutMapping("/saga/compensar/{id}")
    public ResponseEntity<Void> compensarReporte(@PathVariable Long id) {
        mascotaService.compensarReporte(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Función: obtenerTodos
     * Título: Obtener todos los reportes
     * Descripción: Consulta y retorna un listado completo con todos los reportes de mascotas registrados en el sistema, sin filtros aplicados.
     *
     * @return ResponseEntity con una lista de objetos ReporteResponseDTO y un código HTTP 200 (OK).
     */
    @Operation(summary = "Obtener todos los reportes")
    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(mascotaService.obtenerTodosLosReportes());
    }

    /**
     * Función: obtenerPorTipo
     * Título: Filtrar reportes por tipo
     * Descripción: Devuelve una lista de reportes de mascotas filtrados según su clasificación (por ejemplo, "PERDIDA" o "ENCONTRADA").
     *
     * @param tipoReporte Cadena de texto (String) que indica la categoría de reporte que se desea buscar.
     * @return ResponseEntity con una lista de objetos ReporteResponseDTO que coinciden con el tipo solicitado y un código HTTP 200 (OK).
     */
    @Operation(summary = "Filtrar por tipo")
    @GetMapping("/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteResponseDTO>> obtenerPorTipo(@PathVariable String tipoReporte) {
        return ResponseEntity.ok(mascotaService.obtenerReportesPorTipo(tipoReporte));
    }

    /**
     * Función: obtenerPorId
     * Título: Obtener reporte por ID
     * Descripción: Busca un reporte de mascota específico de forma exacta utilizando su identificador único.
     *
     * @param id Identificador único de tipo Long del reporte a consultar.
     * @return ResponseEntity con el objeto ReporteResponseDTO encontrado y un código HTTP 200 (OK).
     */
    @Operation(summary = "Obtener reporte por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.obtenerReportePorId(id));
    }
}