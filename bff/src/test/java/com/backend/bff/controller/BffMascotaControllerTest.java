package com.backend.bff.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import com.backend.bff.service.BffMascotaService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Función: BffMascotaControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador Web de Mascotas (BFF)
 * Descripción: Verifica la correcta exposición y funcionamiento de los endpoints 
 * consumidos por la interfaz web, incluyendo la obtención del dashboard, 
 * detalles consolidados, y la gestión de reportes (creación y eliminación).
 */
@ExtendWith(MockitoExtension.class)
class BffMascotaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BffMascotaService bffService;

    @InjectMocks
    private BffMascotaController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Función: obtenerTodasLasMascotas_DebeRetornarListaYStatus200
     * Título: Validar obtención del dashboard de mascotas
     * Descripción: Comprueba que el controlador responda correctamente a la petición GET 
     * devolviendo una lista de tarjetas (MascotaCardDTO) optimizadas para el frontend 
     * junto con un código de estado HTTP 200 (OK).
     */
    @Test
    void obtenerTodasLasMascotas_DebeRetornarListaYStatus200() throws Exception {
        MascotaCardDTO card = MascotaCardDTO.builder()
                .id(1L)
                .nombre("Firulais")
                .titulo("PERDIDA: Perro Pug")
                .build();

        when(bffService.obtenerDashboard()).thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/web/mascotas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Firulais"));

        verify(bffService, times(1)).obtenerDashboard();
    }

    /**
     * Función: obtenerDetalle_DebeRetornarDetalleYStatus200
     * Título: Validar obtención de detalle de mascota
     * Descripción: Asegura que el controlador reciba el parámetro de ruta (ID), delegue la consulta 
     * al servicio BFF y devuelva el objeto MascotaDetalleCompletoDTO con un código HTTP 200 (OK).
     */
    @Test
    void obtenerDetalle_DebeRetornarDetalleYStatus200() throws Exception {
        MascotaDetalleCompletoDTO detalle = MascotaDetalleCompletoDTO.builder()
                .id(10L)
                .nombre("Michi")
                .especie("Gato")
                .build();

        when(bffService.obtenerDetalleMascota(10L)).thenReturn(detalle);

        mockMvc.perform(get("/api/v1/web/mascotas/detalle/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.nombre").value("Michi"));

        verify(bffService, times(1)).obtenerDetalleMascota(10L);
    }

    /**
     * Función: crearReporte_DebeRetornarStatus202Accepted
     * Título: Validar creación de nuevo reporte
     * Descripción: Verifica que el controlador recepcione exitosamente un payload validado, 
     * procese la solicitud delegando en el servicio, y retorne la confirmación junto 
     * con un código de estado HTTP 202 (ACCEPTED).
     */
    @Test
    void crearReporte_DebeRetornarStatus202Accepted() throws Exception {
        WebReporteRequestDTO requestDto = new WebReporteRequestDTO();

        requestDto.setUsuarioId(5L);
        requestDto.setTipoReporte("PERDIDA");
        requestDto.setEspecie("Perro");
        requestDto.setColor("Café");
        requestDto.setLatitud(10.0);
        requestDto.setLongitud(20.0);
        requestDto.setNombreContacto("Juan Pérez");
        requestDto.setTelefonoContacto("+56912345678");

        requestDto.setNombre("Rex");
        requestDto.setRaza("Pastor Alemán");

        when(bffService.crearNuevoReporte(any(WebReporteRequestDTO.class))).thenReturn("Reporte en proceso");

        mockMvc.perform(post("/api/v1/web/mascotas/reportar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isAccepted()) 
                .andExpect(content().string("Reporte en proceso"));

        verify(bffService, times(1)).crearNuevoReporte(any(WebReporteRequestDTO.class));
    }

    /**
     * Función: eliminarReporte_DebeRetornarStatus204NoContent
     * Título: Validar eliminación de reporte
     * Descripción: Comprueba que el controlador maneje correctamente la petición DELETE 
     * para un ID específico y retorne un estado HTTP 204 (NO CONTENT) tras una eliminación exitosa.
     */
    @Test
    void eliminarReporte_DebeRetornarStatus204NoContent() throws Exception {
        Long reporteId = 15L;
        doNothing().when(bffService).eliminarReporte(reporteId);

        mockMvc.perform(delete("/api/v1/web/mascotas/" + reporteId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bffService, times(1)).eliminarReporte(reporteId);
    }
}