package com.backend.usuarios.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AdminInternalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AdminInternalController adminInternalController;

    private ObjectMapper objectMapper;
    private Usuario usuarioMockAdmin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminInternalController).build();
        objectMapper = new ObjectMapper();

        usuarioMockAdmin = Usuario.builder()
                .id(5L)
                .nombre("Nuevo Registro Admin")
                .correo("nuevoadmin@test.com")
                .tipoUsuario("admin") // El mock simula que se guardó con el rol elegido
                .build();
    }

    @Test
    void crearPorAdmin_Exitoso() throws Exception {
        // Solicitamos explícitamente crear un usuario con tipoUsuario = "admin"
        UsuarioRequestDTO request = new UsuarioRequestDTO("Nuevo Registro Admin", 30, "M", "nuevoadmin@test.com", "123456", "555444333", "url", "CEO", "HQ", "admin");
        when(usuarioService.crearUsuarioAdmin(any(UsuarioRequestDTO.class))).thenReturn(usuarioMockAdmin);

        mockMvc.perform(post("/internal/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoUsuario").value("admin"))
                .andExpect(jsonPath("$.nombre").value("Nuevo Registro Admin"));
    }

    @Test
    void actualizarPorAdmin_Exitoso() throws Exception {
        // Solicitamos actualizar un usuario y forzar el tipoUsuario = "admin"
        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "admin");
        when(usuarioService.actualizarUsuarioPorAdmin(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioMockAdmin);

        mockMvc.perform(put("/internal/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoUsuario").value("admin"));
    }
}