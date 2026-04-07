package com.backend.ms_motor_coincidencias.controller;

import com.backend.ms_motor_coincidencias.client.MascotasClient;
import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.service.CoincidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coincidencias")
@RequiredArgsConstructor
public class CoincidenciaController {

    private final MascotasClient mascotasClient;
    private final CoincidenciaService coincidenciaService;

    @GetMapping("/buscar/{idReporte}")
    public ResponseEntity<List<ResultadoMatchDTO>> buscarMatches(@PathVariable Long idReporte) {

        // 1. Buscamos la mascota perdida usando Feign
        ResultadoMatchDTO mascotaPerdida = mascotasClient.obtenerMascotaPorId(idReporte);

        // 2. Traemos todas las mascotas registradas
        List<ResultadoMatchDTO> todasLasMascotas = mascotasClient.obtenerTodasLasMascotas();

        // 3. Filtramos: Si se perdió, buscamos entre las encontradas (y viceversa).
        // Además excluimos a la misma mascota de la lista.
        String tipoBuscado = mascotaPerdida.getTipoReporte().equalsIgnoreCase("PERDIDA") ? "ENCONTRADA" : "PERDIDA";

        List<ResultadoMatchDTO> candidatas = todasLasMascotas.stream()
                .filter(m -> m.getTipoReporte().equalsIgnoreCase(tipoBuscado))
                .collect(Collectors.toList());

        // 4. Pasamos los datos al motor matemático xd
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, candidatas);

        return ResponseEntity.ok(resultados);
    }
}
