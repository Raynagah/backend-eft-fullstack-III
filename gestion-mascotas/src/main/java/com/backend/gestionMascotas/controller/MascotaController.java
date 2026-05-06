package com.backend.gestionMascotas.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.service.MascotaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
@Tag(name = "Reportes de Mascotas", description = "Operaciones para gestionar el registro, búsqueda y eliminación de reportes")
public class MascotaController {

    private final MascotaService mascotaService;

    @Operation(summary = "Registrar un nuevo reporte (Inicio de Saga)")
    @PostMapping
    public ResponseEntity<ReporteMascota> crearReporte(@Valid @RequestBody ReporteRequestDTO dto) {
        ReporteMascota nuevoReporte = mascotaService.registrarReporte(dto);
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED);
    }

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

    @Operation(summary = "Compensar transacción de reporte")
    @PutMapping("/saga/compensar/{id}")
    public ResponseEntity<Void> compensarReporte(@PathVariable Long id) {
        mascotaService.compensarReporte(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Obtener todos los reportes")
    @GetMapping
    public ResponseEntity<List<ReporteMascota>> obtenerTodos() {
        return ResponseEntity.ok(mascotaService.obtenerTodosLosReportes());
    }

    @Operation(summary = "Filtrar por tipo")
    @GetMapping("/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteMascota>> obtenerPorTipo(@PathVariable String tipoReporte) {
        return ResponseEntity.ok(mascotaService.obtenerReportesPorTipo(tipoReporte));
    }

    @Operation(summary = "Obtener mascota por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ReporteMascota> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.obtenerReportePorId(id));
    }
}