package com.backend.gestionMascotas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

@Tag(name = "Reportes de Mascotas", description = "Operaciones para gestionar el registro, búsqueda y transacciones (Saga) de mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    // Configuración swagger
    @Operation(summary = "Registrar un nuevo reporte (Inicio de Saga)", description = "Crea un reporte vinculado a un usuario e inicia la sincronización de geolocalización (Patrón Saga).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reporte creado e inicio de Saga exitoso"),
            @ApiResponse(responseCode = "400", description = "Error de validación (ej. falta usuarioId o datos inválidos)")
    })
    // Endpoint 1: Crear un nuevo reporte (POST /api/mascotas)
    @PostMapping
    public ResponseEntity<ReporteMascota> crearReporte(@Valid @RequestBody ReporteRequestDTO dto) {

        // Pasamos los datos del DTO a nuestro servicio (que usará el Factory Method)
        ReporteMascota nuevoReporte = mascotaService.registrarReporte(dto);

        // Devolvemos un código 201 (Created) y el objeto guardado
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED);
    }

    // Endpoints para SAGA
    @Operation(summary = "Compensar transacción de reporte", description = "Transacción compensatoria: Cambia el estado del reporte a FAILED_SYNC en caso de que falle el microservicio de geolocalización.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compensación ejecutada correctamente")
    })
    // Endpoint de Compensación (PUT /api/mascotas/saga/compensar/{id})
    @PutMapping("/saga/compensar/{id}")
    public ResponseEntity<Void> compensarReporte(@PathVariable Long id) {
        mascotaService.compensarReporte(id);
        return ResponseEntity.ok().build();
    }
    // ----------------------------------------------


    @Operation(summary = "Obtener todos los reportes", description = "Retorna una lista de todos los reportes registrados.")
    // Endpoint 2: Obtener todos los reportes (GET /api/mascotas)
    @GetMapping
    public ResponseEntity<List<ReporteMascota>> obtenerTodos() {
        List<ReporteMascota> reportes = mascotaService.obtenerTodosLosReportes();
        return ResponseEntity.ok(reportes);
    }

    // Endpoint 3: Filtrar por tipo (GET /api/mascotas/tipo/PERDIDA)
    @Operation(summary = "Filtrar por tipo", description = "Busca reportes según el tipo (ej. PERDIDA, ENCONTRADA).")
    @GetMapping("/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteMascota>> obtenerPorTipo(@PathVariable String tipoReporte) {
        List<ReporteMascota> reportes = mascotaService.obtenerReportesPorTipo(tipoReporte);
        return ResponseEntity.ok(reportes);
    }

    // Configuración swagger
    @Operation(summary = "Obtener mascota por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mascota encontrada"),
            @ApiResponse(responseCode = "404", description = "Mascota no encontrada")
    })
    // Endpoint para obtener una mascota por ID (GET /api/mascotas/{id})
    @GetMapping("/{id}")
    public ResponseEntity<ReporteMascota> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.obtenerReportePorId(id));
    }
}