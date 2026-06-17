package com.backend.notificaciones.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Función: NotificacionControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Notificaciones
 * Descripción: Verifica el correcto funcionamiento de los endpoints expuestos para la 
 * gestión de alertas, abarcando la recepción de coincidencias (matches) de la IA, 
 * consulta, eliminación y actualización del estado de lectura.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private ObjectMapper objectMapper;
    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();
        objectMapper = new ObjectMapper();

        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setMensaje("Tu mascota podría haber sido vista.");
    }

    /**
     * Función: recibirCoincidencias
     * Título: Test de procesamiento de coincidencias (Caso de Éxito)
     * Descripción: Simula una petición POST con un DTO válido proveniente del motor de IA. 
     * Verifica que el controlador procese la solicitud correctamente y retorne un HTTP 200 (OK) 
     * con el mensaje de confirmación esperado.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void recibirCoincidencias() throws Exception {
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(10L);
        matchDTO.setEmailUsuario("usuario@test.com");
        matchDTO.setPorcentajeSimilitud(90.5);
        matchDTO.setTitulo("¡Posible coincidencia!");
        matchDTO.setMensaje("Tu mascota podría haber sido vista.");
        matchDTO.setFotografiaUrl("http://ejemplo.com/foto.jpg");

        List<NotificacionMatchDTO> requestBody = List.of(matchDTO);

        doNothing().when(notificacionService).procesarNotificaciones(any());

        mockMvc.perform(post("/api/notificaciones/procesar-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificaciones procesadas exitosamente."));
    }

    /**
     * Función: marcarLeida
     * Título: Test para marcar notificación como leída (Caso de Éxito)
     * Descripción: Simula una petición PUT a la ruta de actualización de lectura de una 
     * notificación específica. Verifica que retorne un HTTP 200 (OK) y exponga la entidad actualizada.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void marcarLeida() throws Exception {
        when(notificacionService.marcarComoLeida(1L)).thenReturn(notificacionMock);

        // Se corrige a put() ya que el controlador expone @PutMapping
        mockMvc.perform(put("/api/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("¡Posible coincidencia!"));
    }

    /**
     * Función: obtenerPorUsuario
     * Título: Test de obtención de notificaciones por usuario (Caso de Éxito)
     * Descripción: Simula una petición GET pasando el ID del usuario en la ruta. 
     * Verifica que el endpoint retorne un HTTP 200 (OK) y la lista de notificaciones correspondiente.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void obtenerPorUsuario() throws Exception {
        when(notificacionService.obtenerPorUsuario(5L)).thenReturn(List.of(notificacionMock));

        mockMvc.perform(get("/api/notificaciones/usuario/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].mensaje").value("Tu mascota podría haber sido vista."));
    }

    /**
     * Función: eliminarNotificacion
     * Título: Test de eliminación de notificación (Caso de Éxito)
     * Descripción: Simula una petición DELETE para una notificación específica. 
     * Valida que el servicio devuelva correctamente el código HTTP 204 (NO CONTENT).
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void eliminarNotificacion() throws Exception {
        doNothing().when(notificacionService).eliminarNotificacion(1L);

        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Función: obtenerTodas
     * Título: Test de obtención de todas las notificaciones (Caso de Éxito)
     * Descripción: Simula una petición GET a la ruta general de notificaciones. 
     * Verifica que retorne un HTTP 200 (OK) y una colección con todos los registros devueltos por el servicio.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void obtenerTodas() throws Exception {
        when(notificacionService.obtenerTodas()).thenReturn(List.of(notificacionMock));

        mockMvc.perform(get("/api/notificaciones/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}