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

/**
 * Función: CoincidenciaControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Coincidencias
 * Descripción: Verifica el comportamiento de los endpoints de cálculo de matches, validando 
 * el manejo de errores, la recuperación de datos para la UI y la orquestación de 
 * persistencia y notificaciones mediante clientes Feign.
 */
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
    void setUp() {
        reporteOriginal = new ResultadoMatchDTO();
        reporteOriginal.setId(1L);
        reporteOriginal.setTipoReporte("PERDIDA");

        candidataEncontrada = new ResultadoMatchDTO();
        candidataEncontrada.setId(2L);
        candidataEncontrada.setTipoReporte("ENCONTRADA");
        candidataEncontrada.setEmailContacto("contacto@test.com");
        candidataEncontrada.setFotografiaUrl("url-foto");
    }

    /**
     * Función: buscarMatches_IdInvalido_LanzaBadRequestException
     * Título: Validar ID incorrecto
     * Descripción: Comprueba que el controlador arroje una excepción de tipo BadRequestException 
     * al recibir un ID nulo o con valor menor o igual a cero.
     */
    @Test
    void buscarMatches_IdInvalido_LanzaBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(null));
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(0L));
        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> controller.buscarMatches(-5L));

        assertNotNull(ex1);
        assertNotNull(ex2);
        assertNotNull(ex3);
    }

    /**
     * Función: buscarMatches_ReporteNoExiste_LanzaResourceNotFoundException
     * Título: Validar reporte inexistente
     * Descripción: Verifica que si el cliente de mascotas no encuentra el reporte original, 
     * el sistema detenga el proceso lanzando una excepción ResourceNotFoundException.
     */
    @Test
    void buscarMatches_ReporteNoExiste_LanzaResourceNotFoundException() {
        when(mascotasClient.obtenerMascotaPorId(99L)).thenReturn(null);
        
        ResourceNotFoundException ex1 = assertThrows(ResourceNotFoundException.class, () -> controller.buscarMatches(99L));

        assertNotNull(ex1);
    }

    /**
     * Función: buscarMatches_ObtieneResultadosSinModificarEstado
     * Título: Búsqueda de solo lectura para UI
     * Descripción: Asegura que el endpoint de búsqueda únicamente calcule y retorne los 
     * datos sin invocar a los clientes de persistencia o notificación.
     */
    @Test
    void buscarMatches_ObtieneResultadosSinModificarEstado() {
        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(reporteOriginal, candidataEncontrada));
        
        candidataEncontrada.setPorcentajeSimilitud(90.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        ResponseEntity<List<ResultadoMatchDTO>> response = controller.buscarMatches(1L);

        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody().isEmpty());
        verify(coincidenciaRepository, never()).save(any(Coincidencia.class));
        verify(notificacionesClient, never()).enviarNotificaciones(anyList());
    }

    /**
     * Función: procesarYNotificarMatches_ConCoincidenciaAlta_DebeFiltrarGuardarYNotificar
     * Título: Procesar coincidencia alta y notificar
     * Descripción: Valida el flujo completo de procesamiento. Si existe un match por encima 
     * del umbral, el sistema debe guardar la coincidencia y emitir la alerta.
     */
    @Test
    void procesarYNotificarMatches_ConCoincidenciaAlta_DebeFiltrarGuardarYNotificar() {
        ResultadoMatchDTO candidataMala = new ResultadoMatchDTO();

        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(
                List.of(reporteOriginal, candidataEncontrada, candidataMala)
        );

        candidataEncontrada.setPorcentajeSimilitud(90.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        ResponseEntity<String> response = controller.procesarYNotificarMatches(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(coincidenciaRepository, times(1)).save(any(Coincidencia.class));
        verify(notificacionesClient, times(1)).enviarNotificaciones(anyList());
    }

    /**
     * Función: procesarYNotificarMatches_ConCoincidenciaBaja_NoDebeGuardarNiNotificar
     * Título: Descartar coincidencia baja
     * Descripción: Garantiza que las coincidencias calculadas que no alcancen el umbral 
     * mínimo de similitud (85%) no se persistan ni generen envío de correos.
     */
    @Test
    void procesarYNotificarMatches_ConCoincidenciaBaja_NoDebeGuardarNiNotificar() {
        reporteOriginal.setTipoReporte("ENCONTRADA");
        candidataEncontrada.setTipoReporte("PERDIDA");

        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(reporteOriginal, candidataEncontrada));

        candidataEncontrada.setPorcentajeSimilitud(50.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        ResponseEntity<String> response = controller.procesarYNotificarMatches(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(coincidenciaRepository, never()).save(any(Coincidencia.class));
        verify(notificacionesClient, never()).enviarNotificaciones(anyList());
    }

    /**
     * Función: procesarYNotificarMatches_ExcepcionNotificacion_DebeManejarCatchSinRomperRespuesta
     * Título: Tolerancia a fallos en el cliente de notificaciones
     * Descripción: Simula una caída del microservicio de notificaciones y verifica que 
     * el bloque try-catch del controlador intercepte el error, permitiendo retornar una 
     * respuesta HTTP 200 sin romper el flujo principal.
     */
    @Test
    void procesarYNotificarMatches_ExcepcionNotificacion_DebeManejarCatchSinRomperRespuesta() {
        when(mascotasClient.obtenerMascotaPorId(1L)).thenReturn(reporteOriginal);
        when(mascotasClient.obtenerTodasLasMascotas()).thenReturn(List.of(candidataEncontrada));

        candidataEncontrada.setPorcentajeSimilitud(99.0);
        when(coincidenciaService.evaluarCoincidencias(any(), any())).thenReturn(List.of(candidataEncontrada));

        doThrow(new RuntimeException("Microservicio de notificaciones no responde"))
                .when(notificacionesClient).enviarNotificaciones(anyList());

        ResponseEntity<String> response = controller.procesarYNotificarMatches(1L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}