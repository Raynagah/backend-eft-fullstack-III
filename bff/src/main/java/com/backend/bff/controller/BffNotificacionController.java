package com.backend.bff.controller;

import com.backend.bff.service.BffNotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Función: BffNotificacionController (Controlador BFF)
 * Título: Controlador Web de Notificaciones (BFF)
 * Descripción: Expone los endpoints para la gestión de notificaciones de cara al frontend (aplicación web). Permite a los usuarios consultar su bandeja de notificaciones personales, actualizar el estado de lectura de las alertas y eliminar mensajes específicos, delegando la orquestación de estas operaciones al servicio BFF correspondiente.
 */
@RestController
@RequestMapping("/api/v1/web/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BffNotificacionController {

    private final BffNotificacionService bffNotificacionService;

    /**
     * Función: obtenerMisNotificaciones
     * Título: Obtener notificaciones del usuario
     * Descripción: Recupera el listado completo de notificaciones asociadas a un identificador de usuario específico, permitiendo al frontend renderizar el historial de alertas (ej. avisos de mascotas encontradas cerca) del usuario.
     *
     * @param usuarioId Identificador único de tipo Long que representa al usuario del cual se consultarán las notificaciones.
     * @return ResponseEntity con la lista de notificaciones correspondientes al usuario y un código HTTP 200 (OK).
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerMisNotificaciones(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(bffNotificacionService.obtenerMisNotificaciones(usuarioId));
    }

    /**
     * Función: marcarComoLeida
     * Título: Marcar notificación como leída
     * Descripción: Actualiza el estado de una notificación específica para indicarla como "leída", lo cual habitualmente remueve el indicador visual de mensaje nuevo o no leído en la interfaz de usuario.
     *
     * @param id Identificador único de tipo Long de la notificación cuyo estado se desea actualizar.
     * @return ResponseEntity con el objeto o mensaje resultante de la actualización y un código HTTP 200 (OK).
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(bffNotificacionService.marcarComoLeida(id));
    }

    /**
     * Función: eliminarNotificacion
     * Título: Eliminar notificación
     * Descripción: Procesa la solicitud para eliminar permanentemente una alerta o notificación específica del registro del usuario utilizando su identificador único.
     *
     * @param id Identificador único de tipo Long de la notificación que se desea eliminar.
     * @return ResponseEntity vacío (Void) con un código HTTP 204 (NO CONTENT), confirmando que la operación de borrado se realizó de forma exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        bffNotificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}