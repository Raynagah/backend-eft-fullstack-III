package com.backend.usuarios.controller;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    // MockMvc es la herramienta principal que simula peticiones HTTP sin levantar un servidor real
    private MockMvc mockMvc;

    // Simulamos el servicio porque no queremos ir a la base de datos real
    @Mock
    private UsuarioService usuarioService;

    // Inyectamos el servicio falso dentro de nuestro controlador real
    @InjectMocks
    private UsuarioController usuarioController;

    // ObjectMapper nos sirve para convertir objetos Java a texto JSON y viceversa
    private ObjectMapper objectMapper;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc para que apunte solo a nuestro UsuarioController
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        // Creamos un usuario de prueba que reutilizaremos en varios tests
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .correo("juan@test.com")
                .build();
    }

    @Test
    void crearUsuario() throws Exception {
        // 1. PREPARACIÓN: Creamos los datos de entrada y le decimos al servicio falso qué debe responder
        UsuarioRequestDTO request = new UsuarioRequestDTO("Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir");
        when(usuarioService.crearUsuario(any(UsuarioRequestDTO.class))).thenReturn(usuarioMock);

        // 2 & 3. ACCIÓN Y VERIFICACIÓN: Simulamos un POST a /api/usuarios
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Convertimos el objeto 'request' a un string JSON para enviarlo en el body
                        .content(objectMapper.writeValueAsString(request)))
                // Verificamos que el controlador responda con código HTTP 201 (Created)
                .andExpect(status().isCreated())
                // Verificamos que el JSON de respuesta tenga un campo "nombre" con valor "Juan"
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void listarUsuarios() throws Exception {
        // PREPARACIÓN
        when(usuarioService.listar()).thenReturn(List.of(usuarioMock));

        // ACCIÓN Y VERIFICACIÓN: Simulamos un GET
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                // Al ser una lista, usamos $[0] para verificar el primer elemento del JSON
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
        // PREPARACIÓN: Creamos el DTO de respuesta que devolverá el servicio falso
        LoginResponseDTO responseMock = LoginResponseDTO.builder()
                .token("mock_token")
                .sessionId("session_id")
                .build();

        when(usuarioService.login("juan@test.com", "123456")).thenReturn(responseMock);

        // Construimos manualmente el JSON de entrada que espera el controlador
        String loginBody = """
                {
                    "correo": "juan@test.com",
                    "password": "123456"
                }
                """;

        // ACCIÓN Y VERIFICACIÓN
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

        // ACCIÓN Y VERIFICACIÓN: Probamos el uso de Query Parameters (ej: ?id=1&sessionId=sesion_123)
        mockMvc.perform(get("/api/usuarios/validar-sesion")
                        .param("id", "1")
                        .param("sessionId", "sesion_123"))
                .andExpect(status().isOk())
                // Como devuelve un boolean (primitivo) y no un objeto JSON complejo, verificamos el contenido directamente
                .andExpect(content().string("true"));
    }

    @Test
    void eliminarUsuario() throws Exception {
        // PREPARACIÓN: Como eliminarUsuario no devuelve nada (void), usamos doNothing()
        doNothing().when(usuarioService).eliminarUsuario(1L);

        // ACCIÓN Y VERIFICACIÓN
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
        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2");
        // eq(1L) asegura que el mock solo responda si el ID solicitado es exactamente 1
        when(usuarioService.actualizarUsuario(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioMock);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }
}