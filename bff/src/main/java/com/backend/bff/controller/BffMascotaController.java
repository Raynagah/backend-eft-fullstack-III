package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import com.backend.bff.service.BffMascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Función: BffMascotaController (Controlador BFF)
 * Título: Controlador Web de Mascotas (BFF)
 * Descripción: Actúa como intermediario (Backend For Frontend) para el módulo de mascotas en la aplicación web. Expone endpoints optimizados específicamente para el consumo del frontend, consolidando la información de listas (cards) y detalles completos, y delegando la creación y eliminación de reportes a los servicios orquestadores correspondientes.
 */
@RestController
@RequestMapping("/api/v1/web/mascotas")
@RequiredArgsConstructor
public class BffMascotaController {

    private final BffMascotaService bffService;

    /**
     * Función: obtenerTodasLasMascotas
     * Título: Obtener dashboard de mascotas
     * Descripción: Solicita al servicio BFF la información resumida de todas las mascotas registradas, agrupada y optimizada en formato de tarjetas (cards) para su rápida visualización en el feed principal o dashboard de la interfaz de usuario.
     *
     * @return ResponseEntity con una lista de objetos MascotaCardDTO y un código HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<MascotaCardDTO>> obtenerTodasLasMascotas() {
        List<MascotaCardDTO> mascotas = bffService.obtenerDashboard();
        return ResponseEntity.ok(mascotas);
    }

    /**
     * Función: obtenerDetalle
     * Título: Obtener detalle completo de mascota
     * Descripción: Recupera la información íntegra y consolidada de un reporte de mascota específico mediante su identificador. Este endpoint entrega todos los datos necesarios para renderizar la vista de detalle individual en el frontend.
     *
     * @param id Identificador único de tipo Long del reporte de mascota a consultar.
     * @return ResponseEntity con el objeto MascotaDetalleCompletoDTO y un código HTTP 200 (OK).
     */
    @GetMapping("/detalle/{id}")
    public ResponseEntity<MascotaDetalleCompletoDTO> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(bffService.obtenerDetalleMascota(id));
    }

    /**
     * Función: crearReporte
     * Título: Crear nuevo reporte de mascota
     * Descripción: Recepciona y valida estructuralmente los datos enviados desde el formulario web para generar un nuevo reporte de mascota (ya sea perdida o encontrada). Delega el procesamiento de la información al servicio BFF subyacente.
     *
     * @param webDto Objeto WebReporteRequestDTO validado que contiene toda la información ingresada por el usuario para el reporte.
     * @return ResponseEntity con el resultado u objeto generado por la operación y un código HTTP 202 (ACCEPTED), indicando que la solicitud de creación fue aceptada y procesada.
     */
    @PostMapping("/reportar")
    public ResponseEntity<?> crearReporte(@Valid @RequestBody WebReporteRequestDTO webDto) {
        var response = bffService.crearNuevoReporte(webDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     * Función: eliminarReporte
     * Título: Eliminar reporte de mascota
     * Descripción: Recibe la instrucción desde el frontend para eliminar un reporte de mascota específico utilizando su ID. Delega la ejecución del borrado en cascada (u orquestación de borrado) al servicio BFF.
     *
     * @param id Identificador único de tipo Long del reporte que se desea eliminar.
     * @return ResponseEntity vacío (Void) con un código HTTP 204 (NO CONTENT), confirmando que la operación se realizó de forma exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Long id) {
        bffService.eliminarReporte(id);
        return ResponseEntity.noContent().build(); 
    }
}