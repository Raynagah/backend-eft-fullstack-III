package com.backend.notificaciones.controller;

import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Función: NotificacionController (Controlador)
 * Título: Controlador de API de Notificaciones
 * Descripción: Expone los endpoints REST para la gestión de alertas y notificaciones, incluyendo el procesamiento de coincidencias (matches) y la administración del estado de lectura.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de alertas y estados de lectura")
public class NotificacionController {

    // Inyectamos el servicio de notificaciones para manejar la lógica de negocio
    private final NotificacionService notificacionService;

    /**
     * Función: recibirCoincidencias
     * Título: Procesar coincidencias
     * Descripción: Recibe un listado de coincidencias (matches) generadas por el motor de IA, procesándolas para crear y despachar las notificaciones correspondientes a los usuarios implicados.
     *
     * @param dtos Lista de objetos NotificacionMatchDTO validados que contienen la información de las coincidencias.
     * @return ResponseEntity con un mensaje de texto confirmando el procesamiento exitoso y un código HTTP 200 (OK).
     */
    // Endpoint para recibir coincidencias del motor de IA, procesarlas y generar notificaciones para los usuarios
    @Operation(summary = "Procesar coincidencias", description = "Recibe matches del motor de IA.")
    @PostMapping("/procesar-match")
    public ResponseEntity<String> recibirCoincidencias(@Valid @RequestBody List<NotificacionMatchDTO> dtos) {
        notificacionService.procesarNotificaciones(dtos);
        return ResponseEntity.ok("Notificaciones procesadas exitosamente.");
    }

    /**
     * Función: marcarLeida
     * Título: Marcar notificación como leída
     * Descripción: Actualiza el estado de una notificación específica, marcando su atributo de lectura como verdadero (true).
     *
     * @param id Identificador único de tipo Long de la notificación a modificar.
     * @return ResponseEntity con el objeto Notificacion actualizado y un código HTTP 200 (OK).
     */
    // Endpoint para marcar una notificación como leída, cambiando su estado a true en la variable 'leido'  
    @Operation(summary = "Marcar notificación como leída", description = "Cambia el estado de la variable 'leido' a true.")
    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    /**
     * Función: obtenerPorUsuario
     * Título: Obtener notificaciones por ID de usuario
     * Descripción: Realiza una búsqueda y devuelve el listado completo de notificaciones asociadas a un identificador de usuario específico.
     *
     * @param usuarioId Identificador único de tipo Long del usuario dueño de las notificaciones.
     * @return ResponseEntity que contiene una lista de objetos Notificacion y un código HTTP 200 (OK).
     */
    // Endpoint para obtener todas las notificaciones de un usuario específico, utilizando su ID como parámetro de búsqueda
    @Operation(summary = "Obtener por ID de usuario", description = "Búsqueda técnica por ID.")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Notificacion>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificacionService.obtenerPorUsuario(usuarioId));
    }

    /**
     * Función: eliminarNotificacion
     * Título: Eliminar notificación
     * Descripción: Borra de forma permanente una notificación de la base de datos utilizando su identificador único.
     *
     * @param id Identificador único de tipo Long de la notificación que se desea eliminar.
     * @return ResponseEntity vacío (Void) con un código HTTP 204 (NO CONTENT).
     */
    // Endpoint para eliminar una notificación específica por su ID, eliminándola de la base de datos
    @Operation(summary = "Eliminar notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Función: obtenerTodas
     * Título: Obtener todas las notificaciones
     * Descripción: Recupera el registro completo de todas las notificaciones almacenadas en el sistema, sin aplicar filtros. Generalmente utilizado con fines administrativos o de pruebas.
     *
     * @return ResponseEntity con una lista de todos los objetos Notificacion y un código HTTP 200 (OK).
     */
    // Endpoint para obtener todas las notificaciones, principalmente para pruebas y administración, sin filtros específicos
    @Operation(summary = "Obtener todas las notificaciones")
    @GetMapping("/todas")
    public ResponseEntity<List<Notificacion>> obtenerTodas() {
        return ResponseEntity.ok(notificacionService.obtenerTodas());
    }
}