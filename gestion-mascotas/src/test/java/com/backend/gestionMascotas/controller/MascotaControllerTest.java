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

/**
 * Función: MascotaControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Mascotas
 * Descripción: Verifica el comportamiento de los endpoints REST utilizando MockMvc 
 * para simular peticiones HTTP. Asegura que el controlador delegue correctamente la lógica 
 * al servicio y retorne los códigos de estado y payloads esperados.
 */
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
        this.mockMvc = MockMvcBuilders.standaloneSetup(mascotaController).build();
        this.objectMapper = new ObjectMapper();

        responseDTO = new ReporteResponseDTO(
                1L, 125L, "PERDIDA", "Cachupín", "Perro", "Mestizo", "Dorado",
                "Grande", "Juan", "123456", "juan@correo.com", "url", -34.6, -58.3, null, "COMPLETED"
        );
    }

    /**
     * Función: cuandoCrearReporte_entoncesRetornaStatus201YDto
     * Título: Validar creación de reporte (Inicio Saga)
     * Descripción: Comprueba que al recibir una solicitud válida (POST), el controlador 
     * invoque al servicio y retorne el objeto creado junto a un código HTTP 201 (CREATED).
     */
    @Test
    void cuandoCrearReporte_entoncesRetornaStatus201YDto() throws Exception {
        ReporteRequestDTO requestDTO = new ReporteRequestDTO(
                125L,
                "PERDIDA", "Cachupín", "Perro", "Mestizo", "Dorado",
                "Grande", "Juan Pérez", "12345678", "juan@correo.com", "url", -34.6, -58.3
        );
        
        when(mascotaService.registrarReporte(any(ReporteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Cachupín"))
                .andExpect(jsonPath("$.usuarioId").value(125L));

        verify(mascotaService, times(1)).registrarReporte(any(ReporteRequestDTO.class));
    }

    /**
     * Función: cuandoObtenerTodos_entoncesRetornaListaYStatus200
     * Título: Validar obtención general de reportes
     * Descripción: Asegura que el endpoint de consulta sin filtros (GET) retorne 
     * correctamente un arreglo JSON poblado y un código HTTP 200 (OK).
     */
    @Test
    void cuandoObtenerTodos_entoncesRetornaListaYStatus200() throws Exception {
        when(mascotaService.obtenerTodosLosReportes()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cachupín"));

        verify(mascotaService, times(1)).obtenerTodosLosReportes();
    }

    /**
     * Función: cuandoObtenerPorId_entoncesRetornaDTOYStatus200
     * Título: Validar consulta por ID
     * Descripción: Verifica que la búsqueda por identificador (GET) responda con 
     * el reporte exacto y un código HTTP 200 (OK).
     */
    @Test
    void cuandoObtenerPorId_entoncesRetornaDTOYStatus200() throws Exception {
        when(mascotaService.obtenerReportePorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/mascotas/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cachupín"));

        verify(mascotaService, times(1)).obtenerReportePorId(1L);
    }

    /**
     * Función: cuandoObtenerPorTipo_entoncesRetornaListaYStatus200
     * Título: Validar filtrado por tipo de reporte
     * Descripción: Comprueba que el endpoint de filtrado (GET) delegue la categoría 
     * solicitada y retorne la lista correspondiente con un código HTTP 200 (OK).
     */
    @Test
    void cuandoObtenerPorTipo_entoncesRetornaListaYStatus200() throws Exception {
        when(mascotaService.obtenerReportesPorTipo("PERDIDA")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/mascotas/tipo/PERDIDA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoReporte").value("PERDIDA"));

        verify(mascotaService, times(1)).obtenerReportesPorTipo("PERDIDA");
    }

    /**
     * Función: cuandoEliminarReporte_entoncesStatus204
     * Título: Validar eliminación de reporte
     * Descripción: Asegura que al solicitar el borrado de un registro (DELETE), se procese 
     * la operación y el servidor responda con un código HTTP 204 (NO CONTENT).
     */
    @Test
    void cuandoEliminarReporte_entoncesStatus204() throws Exception {
        doNothing().when(mascotaService).eliminarReporte(1L);

        mockMvc.perform(delete("/api/mascotas/1"))
                .andExpect(status().isNoContent()); 

        verify(mascotaService, times(1)).eliminarReporte(1L);
    }

    /**
     * Función: cuandoCompensarReporte_entoncesStatus200
     * Título: Validar endpoint de compensación (Saga)
     * Descripción: Verifica que la ruta encargada de revertir transacciones fallidas 
     * procese la solicitud correctamente y devuelva un código HTTP 200 (OK).
     */
    @Test
    void cuandoCompensarReporte_entoncesStatus200() throws Exception {
        doNothing().when(mascotaService).compensarReporte(1L);

        mockMvc.perform(put("/api/mascotas/saga/compensar/1"))
                .andExpect(status().isOk()); 

        verify(mascotaService, times(1)).compensarReporte(1L);
    }
}