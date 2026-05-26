package com.backend.ms_motor_coincidencias.controller;

import com.backend.ms_motor_coincidencias.client.MascotasClient;
import com.backend.ms_motor_coincidencias.client.NotificacionesClient;
import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.model.Coincidencia;
import com.backend.ms_motor_coincidencias.repository.CoincidenciaRepository;
import com.backend.ms_motor_coincidencias.service.CoincidenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoincidenciaControllerTest {

    // Mis dependencias simuladas (Mocks)
    @Mock
    private MascotasClient mascotasClient;
    @Mock
    private CoincidenciaService coincidenciaService;
    @Mock
    private NotificacionesClient notificacionesClient;
    @Mock
    private CoincidenciaRepository coincidenciaRepository;

    // Inyecto todo en mi controlador
    @InjectMocks
    private CoincidenciaController controller;

    private ResultadoMatchDTO reporteOriginal;
    private ResultadoMatchDTO candidataEncontrada;

    @BeforeEach
    void setUp() {
        // Datos base confiables
        reporteOriginal = new ResultadoMatchDTO();
        reporteOriginal.setReporteId(1L);
        reporteOriginal.setTipoReporte("PERDIDA");

        candidataEncontrada = new ResultadoMatchDTO();
        candidataEncontrada.setReporteId(2L);
        candidataEncontrada.setTipoReporte("ENCONTRADA");
        candidataEncontrada.setEmailContacto("contacto@test.com");
        candidataEncontrada.setFotografiaUrl("url-foto");
    }

    @Test
    void buscarMatches_ConCoincidenciaAlta_DebeGuardarYNotificar() {
        // PREPARACIÓN
        // 1. Feign devuelve los datos
        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(reporteOriginal, candidataEncontrada));

        // Le pongo un puntaje alto para que entre al if (>= 85.0)
        candidataEncontrada.setPorcentajeSimilitud(90.0);

        // 2. Mi servicio evalúa (recibe la original y la filtrada, sin contar la misma id 1L)
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        // ACCIÓN: Llamo al endpoint
        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        // VERIFICACIÓN
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        // Verifico que se guardó el histórico en la BD
        verify(coincidenciaRepository, times(1)).save(any(Coincidencia.class));

        // Verifico que mandó el mail vía Feign
        verify(notificacionesClient, times(1)).enviarNotificaciones(anyList());
    }

    @Test
    void buscarMatches_ConCoincidenciaBaja_NoDebeGuardarNiNotificar() {
        // PREPARACIÓN
        // En este caso es ENCONTRADA, por lo que buscará las PERDIDAS
        reporteOriginal.setTipoReporte("ENCONTRADA");
        candidataEncontrada.setTipoReporte("PERDIDA");

        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(reporteOriginal, candidataEncontrada));

        // Similitud baja, no debe entrar al IF
        candidataEncontrada.setPorcentajeSimilitud(50.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        // ACCIÓN
        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        // VERIFICACIÓN
        assertEquals(200, response.getStatusCode().value());

        // Jamás debió guardar ni notificar
        verify(coincidenciaRepository, never()).save(any(Coincidencia.class));
        verify(notificacionesClient, never()).enviarNotificaciones(anyList());
    }

    @Test
    void buscarMatches_ExcepcionNotificacion_DebeManejarCatchSinRomperRespuesta() {
        // PREPARACIÓN
        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(candidataEncontrada));

        candidataEncontrada.setPorcentajeSimilitud(99.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        // Simulo que el microservicio de notificaciones está caído
        doThrow(new RuntimeException("Microservicio de notificaciones no responde"))
                .when(notificacionesClient).enviarNotificaciones(anyList());

        // ACCIÓN: Hago la petición. Si mi Catch está bien escrito, esto no tirará un Error 500.
        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        // VERIFICACIÓN: Sobrevivió a la caída de Feign y respondió un 200 con la lista de matches
        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody().isEmpty());
    }
}