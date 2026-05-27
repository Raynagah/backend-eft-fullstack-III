package com.backend.ms_motor_coincidencias.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coincidencias")
@RequiredArgsConstructor
@Tag(name = "Motor de Coincidencias", description = "Endpoints para el cruce de datos y generación de alertas")
public class CoincidenciaController {

    private final MascotasClient mascotasClient;
    private final CoincidenciaService coincidenciaService;
    private final NotificacionesClient notificacionesClient;
    private final CoincidenciaRepository coincidenciaRepository;

    @Operation(summary = "Buscar coincidencias para un reporte", 
               description = "Compara un reporte contra la base de datos, guarda el match y notifica si es > 85%")
    @ApiResponse(responseCode = "200", description = "Lista de coincidencias encontradas")
    @GetMapping("/buscar/{idReporte}")
    public ResponseEntity<List<ResultadoMatchDTO>> buscarMatches(@PathVariable Long idReporte) {

        // --- 1. VALIDACIÓN DE ENTRADA ---
        if (idReporte == null || idReporte <= 0) {
            throw new BadRequestException("El ID del reporte debe ser un número positivo mayor a 0.");
        }

        // --- 2. OBTENCIÓN DE DATOS Y VALIDACIÓN DE EXISTENCIA ---
        ResultadoMatchDTO mascotaOriginal = mascotasClient.obtenerMascotaPorId(idReporte);
        
        if (mascotaOriginal == null || mascotaOriginal.getTipoReporte() == null) {
            throw new ResourceNotFoundException("No se encontró ningún reporte válido con el ID: " + idReporte);
        }

        List<ResultadoMatchDTO> todasLasMascotas = mascotasClient.obtenerTodasLasMascotas();

        // --- 3. FILTRADO LÓGICO ---
        String tipoBuscado = mascotaOriginal.getTipoReporte().equalsIgnoreCase("PERDIDA") ? "ENCONTRADA" : "PERDIDA";

        List<ResultadoMatchDTO> candidatas = todasLasMascotas.stream()
                .filter(m -> m.getTipoReporte() != null && m.getTipoReporte().equalsIgnoreCase(tipoBuscado))
                .filter(m -> m.getReporteId() != null && !m.getReporteId().equals(idReporte))
                .collect(Collectors.toList());

        // --- 4. CÁLCULO DE PORCENTAJES ---
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaOriginal, candidatas);

        // --- 5. PERSISTENCIA Y PREPARACIÓN DE ALERTAS ---
        List<NotificacionMatchDTO> alertas = resultados.stream()
                .filter(r -> r.getPorcentajeSimilitud() >= 85.0)
                .map(r -> {
                    Coincidencia coincidencia = new Coincidencia();
                    coincidencia.setReporteOriginalId(idReporte);
                    coincidencia.setReporteEncontradoId(r.getReporteId());
                    coincidencia.setPorcentajeSimilitud(r.getPorcentajeSimilitud());
                    coincidencia.setEmailNotificado(r.getEmailContacto());
                    coincidenciaRepository.save(coincidencia);

                    NotificacionMatchDTO dto = new NotificacionMatchDTO();
                    dto.setReporteId(r.getReporteId());
                    dto.setPorcentajeSimilitud(r.getPorcentajeSimilitud());
                    dto.setFotografiaUrl(r.getFotografiaUrl());
                    dto.setTitulo("¡Posible coincidencia encontrada!");
                    dto.setMensaje("Hay un match del " + r.getPorcentajeSimilitud() + "% con un reporte.");
                    dto.setEmailUsuario(r.getEmailContacto());

                    return dto;
                })
                .collect(Collectors.toList());

        // --- 6. ENVÍO DE NOTIFICACIONES ---
        if (!alertas.isEmpty()) {
            try {
                notificacionesClient.enviarNotificaciones(alertas);
                System.out.println("Éxito: Se enviaron e insertaron " + alertas.size() + " registros.");
            } catch (Exception e) {
                System.err.println("Error de comunicación: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(resultados);
    }
}