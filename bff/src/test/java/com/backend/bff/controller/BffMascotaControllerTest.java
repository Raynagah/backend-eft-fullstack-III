package com.backend.bff.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import com.backend.bff.service.BffMascotaService;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // ==========================================
    // TESTS PARA OBTENER TODAS LAS MASCOTAS (DASHBOARD)
    // ==========================================
    @Test
    void obtenerTodasLasMascotas_DebeRetornarListaYStatus200() throws Exception {
        // Usamos el builder tal como lo tienes en tu Service
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

    // ==========================================
    // TESTS PARA DETALLE DE MASCOTA
    // ==========================================
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

    // ==========================================
    // TESTS PARA CREAR REPORTE
    // ==========================================
    @Test
    void crearReporte_DebeRetornarStatus202Accepted() throws Exception {
        WebReporteRequestDTO requestDto = new WebReporteRequestDTO();

        // --- CAMPOS OBLIGATORIOS (Para pasar el @Valid) ---
        requestDto.setUsuarioId(5L);
        requestDto.setTipoReporte("PERDIDA");
        requestDto.setEspecie("Perro");
        requestDto.setColor("Café");
        requestDto.setLatitud(10.0);
        requestDto.setLongitud(20.0);
        requestDto.setNombreContacto("Juan Pérez");
        requestDto.setTelefonoContacto("+56912345678");

        // --- CAMPOS OPCIONALES ---
        requestDto.setNombre("Rex");
        requestDto.setRaza("Pastor Alemán");

        // Simulamos la respuesta del servicio
        when(bffService.crearNuevoReporte(any(WebReporteRequestDTO.class))).thenReturn("Reporte en proceso");

        mockMvc.perform(post("/api/v1/web/mascotas/reportar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isAccepted()) // ¡Adiós 400, hola 202!
                .andExpect(content().string("Reporte en proceso"));

        verify(bffService, times(1)).crearNuevoReporte(any(WebReporteRequestDTO.class));
    }
}