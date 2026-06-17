package com.backend.bff.controller;

import com.backend.bff.service.BffNotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Función: BffNotificacionControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador Web de Notificaciones (BFF)
 * Descripción: Verifica el correcto funcionamiento de los endpoints encargados de 
 * gestionar la bandeja de alertas del usuario web, asegurando la obtención de listados, 
 * el marcado como leídas y la eliminación exitosa de notificaciones.
 */
@ExtendWith(MockitoExtension.class)
class BffNotificacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BffNotificacionService bffNotificacionService;

    @InjectMocks
    private BffNotificacionController controller;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc en modo standalone inyectando nuestro controlador
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Función: obtenerMisNotificaciones_DebeRetornarListaYStatus200
     * Título: Validar obtención de bandeja de notificaciones
     * Descripción: Comprueba que el controlador responda correctamente a la petición GET 
     * con el ID del usuario, delegue al servicio y retorne un array JSON con el listado 
     * de alertas acompañado de un código de estado HTTP 200 (OK).
     */
    @Test
    void obtenerMisNotificaciones_DebeRetornarListaYStatus200() throws Exception {
        // Simulamos un DTO genérico o respuesta a través de un Map
        Map<String, Object> mockNotificacion = new HashMap<>();
        mockNotificacion.put("id", 100);
        mockNotificacion.put("mensaje", "Mascota vista cerca de tu ubicación");
        mockNotificacion.put("leida", false);

        when(bffNotificacionService.obtenerMisNotificaciones(1L)).thenReturn(List.of(mockNotificacion));

        mockMvc.perform(get("/api/v1/web/notificaciones/usuario/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].mensaje").value("Mascota vista cerca de tu ubicación"))
                .andExpect(jsonPath("$[0].leida").value(false));

        verify(bffNotificacionService, times(1)).obtenerMisNotificaciones(1L);
    }

    /**
     * Función: marcarComoLeida_DebeRetornarOkYStatus200
     * Título: Validar marcado de lectura de notificación
     * Descripción: Verifica que la petición PUT para cambiar el estado de lectura 
     * de una alerta se procese correctamente, delegando la acción por su ID y 
     * regresando una respuesta exitosa (HTTP 200).
     */
    @Test
    void marcarComoLeida_DebeRetornarOkYStatus200() throws Exception {
        Map<String, Object> mockNotificacionLeida = new HashMap<>();
        mockNotificacionLeida.put("id", 10);
        mockNotificacionLeida.put("leida", true);

        when(bffNotificacionService.marcarComoLeida(10L)).thenReturn(mockNotificacionLeida);

        mockMvc.perform(put("/api/v1/web/notificaciones/10/leer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.leida").value(true));

        verify(bffNotificacionService, times(1)).marcarComoLeida(10L);
    }

    /**
     * Función: eliminarNotificacion_DebeRetornarStatus204NoContent
     * Título: Validar borrado de notificación
     * Descripción: Asegura que la instrucción DELETE se comunique exitosamente 
     * al servicio BFF y el controlador cumpla el estándar REST devolviendo 
     * HTTP 204 (NO CONTENT) cuando un recurso se elimina correctamente.
     */
    @Test
    void eliminarNotificacion_DebeRetornarStatus204NoContent() throws Exception {
        Long notificacionId = 55L;

        // Configuramos el mock para que el método void no haga nada (comportamiento esperado)
        doNothing().when(bffNotificacionService).eliminarNotificacion(notificacionId);

        mockMvc.perform(delete("/api/v1/web/notificaciones/" + notificacionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bffNotificacionService, times(1)).eliminarNotificacion(notificacionId);
    }
}