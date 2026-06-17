package com.backend.ms_motor_coincidencias.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ms_motor_coincidencias.client.MascotasClient;
import com.backend.ms_motor_coincidencias.client.NotificacionesClient;
import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.dto.external.NotificacionMatchDTO;
import com.backend.ms_motor_coincidencias.exception.BadRequestException;
import com.backend.ms_motor_coincidencias.exception.ResourceNotFoundException;
import com.backend.ms_motor_coincidencias.model.Coincidencia;
import com.backend.ms_motor_coincidencias.repository.CoincidenciaRepository;
import com.backend.ms_motor_coincidencias.service.CoincidenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Función: CoincidenciaController (Controlador)
 * Título: Controlador del Motor de Coincidencias
 * Descripción: Expone los endpoints REST encargados de orquestar el cruce de datos entre mascotas perdidas y encontradas, calculando porcentajes de similitud y gestionando el envío de alertas mediante la comunicación con otros microservicios (Mascotas y Notificaciones).
 */
@RestController
@RequestMapping("/api/coincidencias")
@RequiredArgsConstructor
@Tag(name = "Motor de Coincidencias", description = "Endpoints para el cruce de datos y generación de alertas")
public class CoincidenciaController {

    private final MascotasClient mascotasClient;
    private final CoincidenciaService coincidenciaService;
    private final NotificacionesClient notificacionesClient;
    private final CoincidenciaRepository coincidenciaRepository;

    /**
     * Función: buscarMatches
     * Título: Buscar coincidencias para UI
     * Descripción: Obtiene un reporte específico y lo compara contra todos los reportes del tipo opuesto (perdida vs encontrada) para calcular y devolver una lista de posibles coincidencias con sus respectivos porcentajes de similitud. Operación de solo lectura (no guarda en BD ni notifica).
     *
     * @param idReporte Identificador único de tipo Long del reporte de mascota base para la búsqueda.
     * @return ResponseEntity que contiene una lista de objetos ResultadoMatchDTO con las coincidencias calculadas y un código HTTP 200 (OK).
     */
    @Operation(summary = "Buscar coincidencias para UI", description = "Solo calcula para mostrar en pantalla, NO notifica.")
    @GetMapping("/buscar/{idReporte}")
    public ResponseEntity<List<ResultadoMatchDTO>> buscarMatches(@PathVariable Long idReporte) {
        if (idReporte == null || idReporte <= 0) throw new BadRequestException("ID inválido.");
        
        ResultadoMatchDTO mascotaOriginal = mascotasClient.obtenerMascotaPorId(idReporte);
        if (mascotaOriginal == null) throw new ResourceNotFoundException("No se encontró reporte: " + idReporte);

        List<ResultadoMatchDTO> todas = mascotasClient.obtenerTodasLasMascotas();
        String tipoBuscado = mascotaOriginal.getTipoReporte().equalsIgnoreCase("PERDIDA") ? "ENCONTRADA" : "PERDIDA";

        List<ResultadoMatchDTO> candidatas = todas.stream()
                .filter(m -> m.getTipoReporte() != null && m.getTipoReporte().equalsIgnoreCase(tipoBuscado))
                .filter(m -> m.getId() != null && !m.getId().equals(idReporte))
                .collect(Collectors.toList());

        // SOLO calcula y retorna, sin guardar en BD ni enviar correos
        return ResponseEntity.ok(coincidenciaService.evaluarCoincidencias(mascotaOriginal, candidatas));
    }

    /**
     * Función: procesarYNotificarMatches
     * Título: Procesar y Notificar Coincidencias
     * Descripción: Evalúa las posibles coincidencias de un reporte dado. Si se detectan matches con un porcentaje de similitud igual o superior al 85%, guarda el registro de la coincidencia en la base de datos local y despacha las alertas a través del cliente Feign de notificaciones.
     *
     * @param idReporte Identificador único de tipo Long del reporte que desencadena el proceso de notificación.
     * @return ResponseEntity con un mensaje de texto detallando el resultado de la operación y la cantidad de notificaciones emitidas, junto con un código HTTP 200 (OK).
     */
    @Operation(summary = "Procesar y Notificar", description = "Calcula, guarda y envía correos. Llamar al crear reporte.")
    @PostMapping("/procesar/{idReporte}")
    public ResponseEntity<String> procesarYNotificarMatches(@PathVariable Long idReporte) {
        // 1. Usamos la misma lógica de búsqueda de arriba
        List<ResultadoMatchDTO> resultados = buscarMatches(idReporte).getBody();
        if (resultados == null || resultados.isEmpty()) return ResponseEntity.ok("No hay coincidencias.");

        // 2. Preparamos alertas y persistimos
        List<NotificacionMatchDTO> alertas = resultados.stream()
                .filter(r -> r.getPorcentajeSimilitud() >= 85.0)
                .map(r -> {
                    Coincidencia coincidencia = new Coincidencia();
                    coincidencia.setReporteOriginalId(idReporte);
                    coincidencia.setReporteEncontradoId(r.getId());
                    coincidencia.setPorcentajeSimilitud(r.getPorcentajeSimilitud());
                    coincidencia.setEmailNotificado(r.getEmailContacto());
                    coincidenciaRepository.save(coincidencia);

                    NotificacionMatchDTO dto = new NotificacionMatchDTO();
                    dto.setReporteId(r.getId());
                    dto.setPorcentajeSimilitud(r.getPorcentajeSimilitud());
                    dto.setFotografiaUrl(r.getFotografiaUrl());
                    dto.setTitulo("¡Posible coincidencia encontrada!");
                    dto.setMensaje("Hay un match del " + r.getPorcentajeSimilitud() + "% con un reporte.");
                    dto.setEmailUsuario(r.getEmailContacto());
                    return dto;
                }).collect(Collectors.toList());

        // 3. Enviamos notificaciones de forma definitiva
        if (!alertas.isEmpty()) {
            try {
                notificacionesClient.enviarNotificaciones(alertas);
            } catch (Exception e) {
                System.err.println("Error de comunicación: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Procesamiento completado. " + alertas.size() + " notificaciones enviadas.");
    }
}