package com.backend.gestionMascotas.controller;

import com.backend.gestionMascotas.dto.ReporteRequestDTO;
import com.backend.gestionMascotas.dto.ReporteResponseDTO;
import com.backend.gestionMascotas.service.MascotaService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MascotaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MascotaService mascotaService;

    @InjectMocks
    private MascotaController mascotaController;

    private ObjectMapper objectMapper;
    private ReporteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Inicializamos MockMvc aislado de Spring
        this.mockMvc = MockMvcBuilders.standaloneSetup(mascotaController).build();

        // ObjectMapper nos servirá para convertir objetos Java a JSON
        this.objectMapper = new ObjectMapper();

        // 💡 CORRECCIÓN: Agregamos el usuarioId (125L) como segundo parámetro del constructor
        responseDTO = new ReporteResponseDTO(
                1L, 125L, "PERDIDA", "Cachupín", "Perro", "Mestizo", "Dorado",
                "Grande", "Juan", "123456", "url", -34.6, -58.3, null, "COMPLETED"
        );
    }

    // --- TEST 1: POST /api/mascotas (Crear Reporte) ---
    @Test
    void cuandoCrearReporte_entoncesRetornaStatus201YDto() throws Exception {
        // Arrange
        ReporteRequestDTO requestDTO = new ReporteRequestDTO(
                125L,
                "PERDIDA", "Cachupín", "Perro", "Mestizo", "Dorado",
                "Grande", "Juan Pérez", "12345678", "juan@correo.com", "url", -34.6, -58.3
        );
        when(mascotaService.registrarReporte(any(ReporteRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cachupín"))
                // Opcional: Podrías validar también que el jsonPath traiga el usuarioId
                .andExpect(jsonPath("$.usuarioId").value(125L));

        verify(mascotaService, times(1)).registrarReporte(any(ReporteRequestDTO.class));
    }

    // --- TEST 2: GET /api/mascotas (Obtener Todos) ---
    @Test
    void cuandoObtenerTodos_entoncesRetornaListaYStatus200() throws Exception {
        when(mascotaService.obtenerTodosLosReportes()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cachupín"));

        verify(mascotaService, times(1)).obtenerTodosLosReportes();
    }

    // --- TEST 3: GET /api/mascotas/{id} (Obtener por ID) ---
    @Test
    void cuandoObtenerPorId_entoncesRetornaDTOYStatus200() throws Exception {
        when(mascotaService.obtenerReportePorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/mascotas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cachupín"));

        verify(mascotaService, times(1)).obtenerReportePorId(1L);
    }

    // --- TEST 4: GET /api/mascotas/tipo/{tipoReporte} (Obtener por Tipo) ---
    @Test
    void cuandoObtenerPorTipo_entoncesRetornaListaYStatus200() throws Exception {
        when(mascotaService.obtenerReportesPorTipo("PERDIDA")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/mascotas/tipo/PERDIDA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoReporte").value("PERDIDA"));

        verify(mascotaService, times(1)).obtenerReportesPorTipo("PERDIDA");
    }

    // --- TEST 5: DELETE /api/mascotas/{id} (Eliminar Reporte) ---
    @Test
    void cuandoEliminarReporte_entoncesStatus204() throws Exception {
        // Como eliminarReporte es void, usamos doNothing()
        doNothing().when(mascotaService).eliminarReporte(1L);

        mockMvc.perform(delete("/api/mascotas/1"))
                .andExpect(status().isNoContent()); // Esperamos HTTP 204

        verify(mascotaService, times(1)).eliminarReporte(1L);
    }

    // --- TEST 6: PUT /api/mascotas/saga/compensar/{id} (Compensar Reporte) ---
    @Test
    void cuandoCompensarReporte_entoncesStatus200() throws Exception {
        doNothing().when(mascotaService).compensarReporte(1L);

        mockMvc.perform(put("/api/mascotas/saga/compensar/1"))
                .andExpect(status().isOk()); // Esperamos HTTP 200

        verify(mascotaService, times(1)).compensarReporte(1L);
    }
}