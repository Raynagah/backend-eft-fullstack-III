package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.AuthService;
import com.backend.bff.service.BffUsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BffUsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BffUsuarioService bffUsuarioService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BffUsuarioController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    // ==========================================
    // TESTS PARA OBTENER PERFIL
    // ==========================================
    @Test
    void obtenerPerfil_DebeRetornarUsuarioYStatus200() throws Exception {
        UsuarioDTO user = new UsuarioDTO();
        user.setId(1L);
        user.setNombre("Carlos");
        user.setEmail("carlos@test.com");

        when(bffUsuarioService.obtenerUsuario(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/web/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Carlos"));

        verify(bffUsuarioService, times(1)).obtenerUsuario(1L);
    }

    // ==========================================
    // TESTS PARA ACTUALIZAR PERFIL
    // ==========================================
    @Test
    void actualizarPerfil_DebeRetornarUsuarioYStatus200() throws Exception {
        UsuarioActualizacionDTO updateDto = new UsuarioActualizacionDTO();
        updateDto.setNombre("Carlos Actualizado");

        UsuarioDTO updatedUser = new UsuarioDTO();
        updatedUser.setId(1L);
        updatedUser.setNombre("Carlos Actualizado");

        // Usamos eq(1L) para asegurar que el ID concuerda con el path variable
        when(bffUsuarioService.actualizarUsuario(eq(1L), any(UsuarioActualizacionDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/web/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos Actualizado"));

        verify(bffUsuarioService, times(1)).actualizarUsuario(eq(1L), any(UsuarioActualizacionDTO.class));
    }

    // ==========================================
    // TESTS PARA OBTENER REPORTES
    // ==========================================
    @Test
    void obtenerMisReportes_DebeRetornarListaYStatus200() throws Exception {
        MascotaCardDTO card = MascotaCardDTO.builder()
                .id(10L)
                .nombre("Michi")
                .titulo("PERDIDA: Gato")
                .build();

        when(bffUsuarioService.obtenerReportesPorUsuario(1L)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/web/usuarios/1/reportes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].nombre").value("Michi"));

        verify(bffUsuarioService, times(1)).obtenerReportesPorUsuario(1L);
    }

    // ==========================================
    // TESTS PARA REGISTRO (CON EXCEPCIÓN FEIGN)
    // ==========================================
    @Test
    void registro_Exitoso_DebeRetornar200() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("nuevo@test.com");

        UsuarioDTO response = new UsuarioDTO();
        response.setId(5L);
        response.setEmail("nuevo@test.com");

        when(authService.registrar(any(UsuarioDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/web/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.correo").value("nuevo@test.com"));

        verify(authService, times(1)).registrar(any(UsuarioDTO.class));
    }

    @Test
    void registro_FallaMicroservicio_DebeRetornarErrorDeFeign() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("duplicado@test.com");

        // Simulamos un error 409 Conflict si el correo ya existe
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(409);
        when(feignException.contentUTF8()).thenReturn("El correo ya está registrado");

        when(authService.registrar(any(UsuarioDTO.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/web/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // Verificamos que respete el 409 que lanza Feign
                .andExpect(content().string("El correo ya está registrado"));
    }
}