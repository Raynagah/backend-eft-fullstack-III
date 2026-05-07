package com.backend.ms_motor_coincidencias.controller;

import com.backend.ms_motor_coincidencias.client.MascotasClient;
import com.backend.ms_motor_coincidencias.client.NotificacionesClient;
import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.dto.external.NotificacionMatchDTO;
import com.backend.ms_motor_coincidencias.service.CoincidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coincidencias")
@RequiredArgsConstructor
@Tag(name = "Motor de Coincidencias", description = "Endpoints para el cruce de datos y generación de alertas")
public class CoincidenciaController {

    private final MascotasClient mascotasClient;
    private final CoincidenciaService coincidenciaService;
    private final NotificacionesClient notificacionesClient;

    @Operation(summary = "Buscar coincidencias para un reporte", 
               description = "Compara un reporte (perdida/encontrada) contra la base de datos y notifica automáticamente si hay match > 85%")
    @ApiResponse(responseCode = "200", description = "Lista de coincidencias encontradas")
    @GetMapping("/buscar/{idReporte}")
    public ResponseEntity<List<ResultadoMatchDTO>> buscarMatches(@PathVariable Long idReporte) {

        // 1. Obtención de datos vía Feign
        ResultadoMatchDTO mascotaOriginal = mascotasClient.obtenerMascotaPorId(idReporte);
        List<ResultadoMatchDTO> todasLasMascotas = mascotasClient.obtenerTodasLasMascotas();

        // 2. Filtrado de lógica de negocio (Opuestos)
        String tipoBuscado = mascotaOriginal.getTipoReporte().equalsIgnoreCase("PERDIDA") ? "ENCONTRADA" : "PERDIDA";

        List<ResultadoMatchDTO> candidatas = todasLasMascotas.stream()
                .filter(m -> m.getTipoReporte().equalsIgnoreCase(tipoBuscado))
                .filter(m -> !m.getReporteId().equals(idReporte))
                .collect(Collectors.toList());

        // 3. Cálculo de porcentajes
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaOriginal, candidatas);

        // 4. Mapeo dinámico a Notificaciones
        List<NotificacionMatchDTO> alertas = resultados.stream()
                .filter(r -> r.getPorcentajeSimilitud() >= 85.0)
                // Eliminamos el filtro de tipo si queremos que notifique a ambos,
                // o nos aseguramos de que estamos notificando al dueño de la mascota PERDIDA
                .map(r -> {
                    NotificacionMatchDTO dto = new NotificacionMatchDTO();
                    // USAMOS getReporteId() que es donde ResultadoMatchDTO guardó el "id" del JSON
                    dto.setReporteId(r.getReporteId());
                    dto.setPorcentajeSimilitud(r.getPorcentajeSimilitud());
                    dto.setFotografiaUrl(r.getFotografiaUrl());
                    dto.setTitulo("¡Posible coincidencia encontrada!");
                    dto.setMensaje("Hay un match del " + r.getPorcentajeSimilitud() + "% con un reporte.");

                    // IMPORTANTE: El MS Notificaciones espera emailUsuario, asegúrate de enviarlo
                    dto.setEmailUsuario(r.getEmailContacto());

                    return dto;
                })
                .collect(Collectors.toList());

        // 5. Envío al MS de Notificaciones
        if (!alertas.isEmpty()) {
            try {
                notificacionesClient.enviarNotificaciones(alertas);
                System.out.println("Éxito: Se enviaron " + alertas.size() + " alertas.");
            } catch (Exception e) {
                System.err.println("Error de comunicación: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(resultados);
    }
}