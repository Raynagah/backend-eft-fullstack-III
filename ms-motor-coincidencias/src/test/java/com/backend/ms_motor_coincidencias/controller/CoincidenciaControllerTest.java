package com.backend.ms_motor_coincidencias.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.backend.ms_motor_coincidencias.client.MascotasClient;
import com.backend.ms_motor_coincidencias.client.NotificacionesClient;
import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.exception.BadRequestException;
import com.backend.ms_motor_coincidencias.exception.ResourceNotFoundException;
import com.backend.ms_motor_coincidencias.model.Coincidencia;
import com.backend.ms_motor_coincidencias.repository.CoincidenciaRepository;
import com.backend.ms_motor_coincidencias.service.CoincidenciaService;

@ExtendWith(MockitoExtension.class)
class CoincidenciaControllerTest {

    @Mock
    private MascotasClient mascotasClient;
    @Mock
    private CoincidenciaService coincidenciaService;
    @Mock
    private NotificacionesClient notificacionesClient;
    @Mock
    private CoincidenciaRepository coincidenciaRepository;

    @InjectMocks
    private CoincidenciaController controller;

    private ResultadoMatchDTO reporteOriginal;
    private ResultadoMatchDTO candidataEncontrada;

    @BeforeEach
    @SuppressWarnings("unused") 
    void setUp() {
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
    void buscarMatches_IdInvalido_LanzaBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(null));
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(0L));
        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(-5L));

        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }

    @Test
    void buscarMatches_ReporteNoExisteOTipoNulo_LanzaResourceNotFoundException() {
        when(mascotasClient.obtenerMascotaPorId(99L)).thenReturn(null);
        ResourceNotFoundException ex1 = assertThrows(ResourceNotFoundException.class, () -> controller.buscarMatches(99L));

        ResultadoMatchDTO sinTipo = new ResultadoMatchDTO();
        sinTipo.setReporteId(100L);
        when(mascotasClient.obtenerMascotaPorId(100L)).thenReturn(sinTipo);
        ResourceNotFoundException ex2 = assertThrows(ResourceNotFoundException.class, () -> controller.buscarMatches(100L));

        assertNotNull(ex1);
        assertNotNull(ex2);
    }

    @Test
    void buscarMatches_ConCoincidenciaAltaYDatosIncompletosEnLista_DebeFiltrarGuardarYNotificar() {
        // Agregamos elementos incompletos para forzar las ramas nulas de los .filter() del Stream
        ResultadoMatchDTO candidataMala1 = new ResultadoMatchDTO(); // Tipo nulo
        ResultadoMatchDTO candidataMala2 = new ResultadoMatchDTO();
        candidataMala2.setTipoReporte("ENCONTRADA"); // ReporteId nulo

        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(
                List.of(reporteOriginal, candidataEncontrada, candidataMala1, candidataMala2)
        );

        candidataEncontrada.setPorcentajeSimilitud(90.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(coincidenciaRepository, times(1)).save(any(Coincidencia.class));
        verify(notificacionesClient, times(1)).enviarNotificaciones(anyList());
    }

    @Test
    void buscarMatches_ConCoincidenciaBaja_NoDebeGuardarNiNotificar() {
        reporteOriginal.setTipoReporte("ENCONTRADA");
        candidataEncontrada.setTipoReporte("PERDIDA");

        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(reporteOriginal, candidataEncontrada));

        candidataEncontrada.setPorcentajeSimilitud(50.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(coincidenciaRepository, never()).save(any(Coincidencia.class));
        verify(notificacionesClient, never()).enviarNotificaciones(anyList());
    }

    @Test
    void buscarMatches_ExcepcionNotificacion_DebeManejarCatchSinRomperRespuesta() {
        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(candidataEncontrada));

        candidataEncontrada.setPorcentajeSimilitud(99.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        doThrow(new RuntimeException("Microservicio de notificaciones no responde"))
                .when(notificacionesClient).enviarNotificaciones(anyList());

        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody().isEmpty());
    }
}