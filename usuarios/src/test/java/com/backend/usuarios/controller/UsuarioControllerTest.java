package com.backend.usuarios.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .correo("juan@test.com")
                .tipoUsuario("cliente")
                .build();
    }

    @Test
    void crearUsuario() throws Exception {
        // Agregado el campo obligatorio "cliente" al final de los argumentos del constructor del record
        UsuarioRequestDTO request = new UsuarioRequestDTO("Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir", "cliente");
        when(usuarioService.crearUsuario(any(UsuarioRequestDTO.class))).thenReturn(usuarioMock);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void listarUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(usuarioMock));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void obtenerPorId() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioMock);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void login() throws Exception {
        LoginResponseDTO responseMock = LoginResponseDTO.builder()
                .token("mock_token")
                .sessionId("session_id")
                .build();

        when(usuarioService.login("juan@test.com", "123456")).thenReturn(responseMock);

        String loginBody = """
                {
                    "correo": "juan@test.com",
                    "password": "123456"
                }
                """;

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_token"))
                .andExpect(jsonPath("$.sessionId").value("session_id"));
    }

    @Test
    void validarSesion() throws Exception {
        when(usuarioService.isSesionValida(1L, "sesion_123")).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/validar-sesion")
                        .param("id", "1")
                        .param("sessionId", "sesion_123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void eliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario eliminado correctamente"));
    }

    @Test
    void logout() throws Exception {
        doNothing().when(usuarioService).logout("sesion_123");

        mockMvc.perform(post("/api/usuarios/logout")
                        .param("sessionId", "sesion_123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Se ha cerrado sesión"));
    }

    @Test
    void actualizarUsuario() throws Exception {
        // Agregado el campo obligatorio "cliente" al final
        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");
        when(usuarioService.actualizarUsuario(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioMock);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }
}