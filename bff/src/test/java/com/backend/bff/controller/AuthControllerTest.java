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

/**
 * Función: AuthControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Autenticación (BFF)
 * Descripción: Verifica los endpoints públicos del Backend For Frontend, garantizando 
 * que el enrutamiento hacia el microservicio de autenticación, el manejo de errores de red (Feign), 
 * y las políticas de seguridad (borrado de contraseñas, inyección de roles) funcionen como se espera.
 */
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
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * Función: login_Exitoso_DebeRetornar200YToken
     * Título: Validar inicio de sesión exitoso
     * Descripción: Comprueba que al proporcionar credenciales válidas, el controlador retorne 
     * un código HTTP 200 (OK) junto con el token de acceso generado por el servicio subyacente.
     */
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

    /**
     * Función: login_FallaMicroservicio_DebeRetornarErrorDeFeign
     * Título: Validar propagación de error en login
     * Descripción: Asegura que si el microservicio de usuarios rechaza la autenticación (ej. 401 Unauthorized), 
     * el BFF intercepte la FeignException y propague el mismo código HTTP y mensaje hacia el cliente.
     */
    @Test
    void login_FallaMicroservicio_DebeRetornarErrorDeFeign() throws Exception {
        LoginRequest request = new LoginRequest();

        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(401);
        when(feignException.contentUTF8()).thenReturn("Credenciales incorrectas");

        when(authService.autenticar(any(LoginRequest.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) 
                .andExpect(content().string("Credenciales incorrectas"));
    }

    /**
     * Función: login_FalloFeignStatusInvalido_FuerzaRetornoHttp500
     * Título: Fallo de login por status Feign desconocido
     * Descripción: Evalúa la rama donde Feign retorna un status inválido (-1),
     * asegurando que el controlador lo intercepte y asigne un error HTTP 500 en lugar
     * de retornar un estatus que rompa el protocolo HTTP.
     */
    @Test
    void login_FalloFeignStatusInvalido_FuerzaRetornoHttp500() throws Exception {
        LoginRequest request = new LoginRequest();

        FeignException feignException = mock(FeignException.class);
        // Provocamos que devuelva -1 para forzar la entrada a la condición (status < 100)
        when(feignException.status()).thenReturn(-1);
        when(feignException.contentUTF8()).thenReturn("Error de conexión de red");

        when(authService.autenticar(any(LoginRequest.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error de conexión de red"));
    }

    /**
     * Función: registro_Exitoso_DebeRetornar200YBorrarPassword
     * Título: Validar registro y sanitización de datos
     * Descripción: Verifica que tras crear un usuario exitosamente, el controlador retorne el código HTTP 200 (OK) 
     * y elimine la contraseña del objeto de respuesta por motivos de seguridad antes de enviarlo al frontend.
     */
    @Test
    void registro_Exitoso_DebeRetornar200YBorrarPassword() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("secreta");

        UsuarioDTO response = new UsuarioDTO();
        response.setEmail("nuevo@test.com");
        response.setPassword("secreta"); 

        when(authService.registrar(any(UsuarioDTO.class))).thenReturn(response);

        String responseBody = mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@test.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(responseBody.contains("secreta"), "La contraseña no debe enviarse al frontend");
    }

    /**
     * Función: registro_Exitoso_NuevoUsuarioNulo
     * Título: Validar registro con respuesta vacía
     * Descripción: Evalúa la tolerancia a fallos del sistema si el microservicio subyacente 
     * no retorna el objeto de usuario tras un registro teóricamente exitoso.
     */
    @Test
    void registro_Exitoso_NuevoUsuarioNulo() throws Exception {
        UsuarioDTO request = new UsuarioDTO();

        when(authService.registrar(any(UsuarioDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    /**
     * Función: registro_FallaMicroservicio_DebeRetornarErrorDeFeign
     * Título: Validar propagación de error en registro
     * Descripción: Comprueba que ante un fallo en el microservicio de usuarios (ej. correo duplicado con error 400), 
     * el controlador capture la excepción de Feign y propague fielmente el error al cliente.
     */
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
                .andExpect(status().isBadRequest()) 
                .andExpect(content().string("El correo ya existe"));
    }

    /**
     * Función: logout_Exitoso_DebeRetornar200
     * Título: Validar cierre de sesión
     * Descripción: Verifica que el controlador capture correctamente el ID de sesión enviado 
     * a través de las cabeceras HTTP y delegue la destrucción de dicha sesión al servicio.
     */
    @Test
    void logout_Exitoso_DebeRetornar200() throws Exception {
        doNothing().when(authService).cerrarSesion("mi-session-id");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-Session-ID", "mi-session-id"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sesión cerrada"));

        verify(authService, times(1)).cerrarSesion("mi-session-id");
    }

    /**
     * Función: login_ExcepcionInesperada_DebeRetornar500
     * Título: Error genérico inesperado en login
     * Descripción: Simula un fallo de sistema genérico (RuntimeException) que escapa a Feign,
     * verificando que el catch global lo atrape y devuelva un código HTTP 500.
     */
    @Test
    void login_ExcepcionInesperada_DebeRetornar500() throws Exception {
        LoginRequest request = new LoginRequest();
        // Simulamos una Exception genérica (no FeignException)
        when(authService.autenticar(any(LoginRequest.class))).thenThrow(new RuntimeException("Error fatal"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error inesperado en el BFF: Error fatal"));
    }
}