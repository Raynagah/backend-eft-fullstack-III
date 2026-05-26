package com.backend.bff.controller;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.AuthService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc en modo "standalone" para aislar el controlador
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper(); // Para convertir objetos a JSON
    }

    // ==========================================
    // TESTS PARA LOGIN
    // ==========================================
    @Test
    void login_Exitoso_DebeRetornar200YToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("test@test.com");
        request.setPassword("12345");

        LoginResponse response = new LoginResponse();
        response.setToken("mi-token-falso");

        when(authService.autenticar(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mi-token-falso"));
    }

    @Test
    void login_FallaMicroservicio_DebeRetornarErrorDeFeign() throws Exception {
        LoginRequest request = new LoginRequest();

        // Simulamos la excepción de Feign que lanzaría el microservicio
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(401);
        when(feignException.contentUTF8()).thenReturn("Credenciales incorrectas");

        when(authService.autenticar(any(LoginRequest.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(content().string("Credenciales incorrectas"));
    }

    // ==========================================
    // TESTS PARA REGISTRO
    // ==========================================
    @Test
    void registro_Exitoso_DebeRetornar200YBorrarPassword() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("secreta");

        UsuarioDTO response = new UsuarioDTO();
        response.setEmail("nuevo@test.com");
        response.setPassword("secreta"); // Simulamos que el service devuelve el pass

        when(authService.registrar(any(UsuarioDTO.class))).thenReturn(response);

        // Capturamos la respuesta entera como un String
        String responseBody = mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@test.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verificamos que la contraseña original se borró correctamente
        // No importa si Jackson manda "null" o si oculta el campo, la contraseña NO debe estar.
        assertFalse(responseBody.contains("secreta"), "La contraseña no debe enviarse al frontend");
    }

    @Test
    void registro_Exitoso_NuevoUsuarioNulo() throws Exception {
        // Probamos la rama donde el service devuelve null para cubrir el if (nuevoUsuario != null)
        UsuarioDTO request = new UsuarioDTO();

        when(authService.registrar(any(UsuarioDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void registro_FallaMicroservicio_DebeRetornarErrorDeFeign() throws Exception {
        UsuarioDTO request = new UsuarioDTO();

        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);
        when(feignException.contentUTF8()).thenReturn("El correo ya existe");

        when(authService.registrar(any(UsuarioDTO.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(content().string("El correo ya existe"));
    }

    // ==========================================
    // TESTS PARA LOGOUT
    // ==========================================
    @Test
    void logout_Exitoso_DebeRetornar200() throws Exception {
        doNothing().when(authService).cerrarSesion("mi-session-id");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-Session-ID", "mi-session-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sesión cerrada"));

        verify(authService, times(1)).cerrarSesion("mi-session-id");
    }
}