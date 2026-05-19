package com.backend.notificaciones.controller;

import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    // Configuro MockMvc para simular peticiones a mis endpoints
    private MockMvc mockMvc;

    // Simulo mi servicio para aislar la prueba solo al controlador
    @Mock
    private NotificacionService notificacionService;

    // Inyecto el servicio falso en mi controlador real
    @InjectMocks
    private NotificacionController notificacionController;

    private ObjectMapper objectMapper;
    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();
        objectMapper = new ObjectMapper();

        // Creo una notificación de prueba para usarla en mis respuestas
        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setMensaje("Tu mascota podría haber sido vista.");
    }

    @Test
    void recibirCoincidencias() throws Exception {
        // PREPARACIÓN: Armo la lista de DTOs llenando los campos obligatorios
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(10L);
        matchDTO.setEmailUsuario("usuario@test.com"); // <-- ¡Esta era la pieza faltante que causaba el 400!
        matchDTO.setPorcentajeSimilitud(90.5);

        // Estos no son obligatorios según tu DTO, pero es buena práctica mandarlos
        matchDTO.setTitulo("¡Posible coincidencia!");
        matchDTO.setMensaje("Tu mascota podría haber sido vista.");
        matchDTO.setFotografiaUrl("http://ejemplo.com/foto.jpg");

        List<NotificacionMatchDTO> requestBody = List.of(matchDTO);

        // Como mi servicio devuelve void, le digo que no haga nada cuando lo llamen
        doNothing().when(notificacionService).procesarNotificaciones(any());

        // ACCIÓN Y VERIFICACIÓN: Ahora sí debería dar 200 OK
        mockMvc.perform(post("/api/notificaciones/procesar-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificaciones procesadas exitosamente."));
    }

    @Test
    void marcarLeida() throws Exception {
        // PREPARACIÓN: Le digo a mi servicio que devuelva mi notificación de prueba
        when(notificacionService.marcarComoLeida(1L)).thenReturn(notificacionMock);

        // ACCIÓN Y VERIFICACIÓN: Hago un PATCH y valido el JSON devuelto
        mockMvc.perform(patch("/api/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("¡Posible coincidencia!"));
    }

    @Test
    void obtenerPorUsuario() throws Exception {
        // PREPARACIÓN
        when(notificacionService.obtenerPorUsuario(5L)).thenReturn(List.of(notificacionMock));

        // ACCIÓN Y VERIFICACIÓN: Hago un GET pasando el ID del usuario en la ruta
        mockMvc.perform(get("/api/notificaciones/usuario/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].mensaje").value("Tu mascota podría haber sido vista."));
    }

    @Test
    void eliminarNotificacion() throws Exception {
        // PREPARACIÓN
        doNothing().when(notificacionService).eliminarNotificacion(1L);

        // ACCIÓN Y VERIFICACIÓN: Hago el DELETE y espero un código HTTP 204 (No Content)
        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerTodas() throws Exception {
        // PREPARACIÓN
        when(notificacionService.obtenerTodas()).thenReturn(List.of(notificacionMock));

        // ACCIÓN Y VERIFICACIÓN
        mockMvc.perform(get("/api/notificaciones/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}