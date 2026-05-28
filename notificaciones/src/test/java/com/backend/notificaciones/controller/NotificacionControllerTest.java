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

    // Configurar MockMvc para simular peticiones a endpoints
    private MockMvc mockMvc;

    // Simular el servicio para aislar la prueba solo al controlador
    @Mock
    private NotificacionService notificacionService;

    // Inyectar el servicio falso en el controlador real
    @InjectMocks
    private NotificacionController notificacionController;

    private ObjectMapper objectMapper;
    private Notificacion notificacionMock;

    // Configuración inicial antes de cada test, creando un MockMvc con el controlador y un ObjectMapper para convertir objetos a JSON
    // También se crea una notificación de prueba para usar en los tests que requieren un objeto Notificación
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();
        objectMapper = new ObjectMapper();

        // Crear una notificación de prueba para usar en los tests que requieren un objeto Notificación
        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setMensaje("Tu mascota podría haber sido vista.");
    }

    // Test para el endpoint que recibe coincidencias del motor de IA, verificando que el controlador 
    // responda correctamente al recibir un DTO válido y que el servicio sea llamado para procesar las notificaciones
    @Test
    void recibirCoincidencias() throws Exception {
        // PREPARACIÓN: Crear un DTO de coincidencia con los campos necesarios para que el endpoint lo acepte sin errores de validación
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(10L);
        matchDTO.setEmailUsuario("usuario@test.com");
        matchDTO.setPorcentajeSimilitud(90.5);

        // Agregar campos opcionales para asegurar de que el DTO es completo y no cause errores de validación
        matchDTO.setTitulo("¡Posible coincidencia!");
        matchDTO.setMensaje("Tu mascota podría haber sido vista.");
        matchDTO.setFotografiaUrl("http://ejemplo.com/foto.jpg");

        List<NotificacionMatchDTO> requestBody = List.of(matchDTO);

        // PREPARACIÓN: Configurar el servicio para que no haga nada cuando se llame al método de procesamiento, ya que 
        // aquí solo queremos probar el controlador y su respuesta
        doNothing().when(notificacionService).procesarNotificaciones(any());

        // ACCIÓN Y VERIFICACIÓN: Hacer un POST al endpoint con el DTO de coincidencia y verificar que la respuesta 
        // sea 200 OK con el mensaje esperado
        mockMvc.perform(post("/api/notificaciones/procesar-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Notificaciones procesadas exitosamente."));
    }

    // Test para el endpoint que marca una notificación como leída, verificando que el controlador responda correctamente
    // al recibir una solicitud para marcar una notificación específica como leída, y que el
    // servicio devuelva la notificación actualizada con el estado de lectura cambiado a true
    @Test
    void marcarLeida() throws Exception {
        // PREPARACIÓN: Configurar el servicio para que devuelva la notificación de prueba cuando se intente marcar como leída
        when(notificacionService.marcarComoLeida(1L)).thenReturn(notificacionMock);

        // ACCIÓN Y VERIFICACIÓN: Hacer un PATCH al endpoint para marcar la notificación como leída y verificar que la respuesta
        // contenga los datos de la notificación esperada
        mockMvc.perform(patch("/api/notificaciones/1/leer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("¡Posible coincidencia!"));
    }

    // Test para el endpoint que obtiene todas las notificaciones de un usuario específico, verificando que el controlador responda correctamente
    // al recibir una solicitud con el ID del usuario, y que el servicio devuelva una
    // lista de notificaciones asociadas a ese usuario, verificando que la respuesta contenga los datos esperados
    @Test
    void obtenerPorUsuario() throws Exception {
        // PREPARACIÓN: Configurar el servicio para que devuelva una lista con la notificación de 
        // prueba cuando se intente obtener las notificaciones por ID de usuario
        when(notificacionService.obtenerPorUsuario(5L)).thenReturn(List.of(notificacionMock));

        // ACCIÓN Y VERIFICACIÓN: Hacer un GET al endpoint para obtener las notificaciones del usuario y verificar que la respuesta
        // contenga la lista de notificaciones esperada
        mockMvc.perform(get("/api/notificaciones/usuario/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].mensaje").value("Tu mascota podría haber sido vista."));
    }

    // Test para el endpoint que elimina una notificación específica por su ID, verificando que el controlador responda correctamente
    // al recibir una solicitud para eliminar una notificación, y que el servicio ejecute la
    // eliminación sin errores, verificando que la respuesta tenga el código HTTP 204 (No Content) que indica que la eliminación fue exitosa
    @Test
    void eliminarNotificacion() throws Exception {
        // PREPARACIÓN: Configurar el servicio para que no haga nada cuando se intente eliminar una 
        // notificación, ya que aquí solo queremos probar el controlador y su respuesta
        doNothing().when(notificacionService).eliminarNotificacion(1L);

        // ACCIÓN Y VERIFICACIÓN: Hacer un DELETE al endpoint para eliminar la notificación 
        // y verificar que la respuesta tenga el código HTTP 204 (No Content)
        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isNoContent());
    }


    // Test para el endpoint que obtiene todas las notificaciones, verificando que el controlador responda correctamente
    // al recibir una solicitud para obtener todas las notificaciones, y que el servicio devuelva una
    // lista de todas las notificaciones almacenadas, verificando que la respuesta contenga los datos esperados
    @Test
    void obtenerTodas() throws Exception {
        // PREPARACIÓN: Configurar el servicio para que devuelva una lista con la notificación de
        //  prueba cuando se intente obtener todas las notificaciones
        when(notificacionService.obtenerTodas()).thenReturn(List.of(notificacionMock));

        // ACCIÓN Y VERIFICACIÓN: Hacer un GET al endpoint para obtener todas las notificaciones y verificar que la respuesta
        // contenga la lista de notificaciones esperada
        mockMvc.perform(get("/api/notificaciones/todas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}