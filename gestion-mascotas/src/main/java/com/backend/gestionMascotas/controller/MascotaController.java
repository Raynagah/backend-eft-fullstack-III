package com.backend.gestionMascotas.controller;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Indica que esta clase maneja peticiones web y devuelve JSON
@RequestMapping("/api/mascotas") // La ruta base para todos los endpoints de este archivo
@RequiredArgsConstructor // Lombok inyecta el servicio automáticamente
public class MascotaController {

    private final MascotaService mascotaService;

    // Endpoint 1: Crear un nuevo reporte (POST /api/mascotas)
    @PostMapping
    public ResponseEntity<ReporteMascota> crearReporte(@RequestBody ReporteRequestDTO dto) {

        // Pasamos los datos del DTO a nuestro servicio (que usará el Factory Method)
        ReporteMascota nuevoReporte = mascotaService.registrarReporte(
                dto.tipoReporte(),
                dto.especie(),
                dto.raza(),
                dto.color(),
                dto.tamano(),
                dto.nombreContacto(),
                dto.telefonoContacto(),
                dto.fotografiaUrl(),
                dto.latitud(),
                dto.longitud()
        );

        // Devolvemos un código 201 (Created) y el objeto guardado
        return new ResponseEntity<>(nuevoReporte, HttpStatus.CREATED);
    }

    // Endpoint 2: Obtener todos los reportes (GET /api/mascotas)
    @GetMapping
    public ResponseEntity<List<ReporteMascota>> obtenerTodos() {
        List<ReporteMascota> reportes = mascotaService.obtenerTodosLosReportes();
        return ResponseEntity.ok(reportes); // Devuelve código 200 (OK)
    }

    // Endpoint 3: Filtrar por tipo (GET /api/mascotas/tipo/PERDIDA)
    @GetMapping("/tipo/{tipoReporte}")
    public ResponseEntity<List<ReporteMascota>> obtenerPorTipo(@PathVariable String tipoReporte) {
        List<ReporteMascota> reportes = mascotaService.obtenerReportesPorTipo(tipoReporte);
        return ResponseEntity.ok(reportes);
    }
}