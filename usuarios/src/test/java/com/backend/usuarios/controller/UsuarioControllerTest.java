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
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Función: UsuarioControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de API de Usuarios
 * Descripción: Verifica el correcto funcionamiento de los endpoints expuestos para la 
 * gestión integral de usuarios, validando que el controlador responda con los códigos HTTP 
 * adecuados y los objetos esperados en operaciones CRUD, autenticación y manejo de sesiones.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;
    private UsuarioDTO usuarioMockDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();
        
        // Instanciamos el DTO de respuesta mockeado
        usuarioMockDTO = new UsuarioDTO(
                1L,
                "Juan",
                "123456789",
                "juan@test.com",
                25,
                "M",
                "Dir",
                "Dev",
                "url",
                "cliente"
        );
    }

    /**
     * Función: crearUsuario
     * Título: Test de creación de usuario (Caso de Éxito)
     * Descripción: Simula una petición POST válida para registrar un nuevo usuario. 
     * Verifica que el endpoint retorne un status 201 (CREATED) y que el JSON de respuesta 
     * contenga el nombre del usuario creado.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void crearUsuario() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir", "cliente");

        when(usuarioService.crearUsuario(any(UsuarioRequestDTO.class))).thenReturn(usuarioMockDTO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    /**
     * Función: listarUsuarios
     * Título: Test de listado de usuarios (Caso de Éxito)
     * Descripción: Simula una petición GET para obtener todos los usuarios. 
     * Verifica que el endpoint retorne un status 200 (OK) y una lista que contenga los datos esperados.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void listarUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(usuarioMockDTO));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    /**
     * Función: obtenerPorId
     * Título: Test de obtención de usuario por ID (Caso de Éxito)
     * Descripción: Simula una petición GET pasando un identificador en la ruta. 
     * Verifica que el endpoint devuelva status 200 (OK) y el ID del usuario coincida con el solicitado.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void obtenerPorId() throws Exception {
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuarioMockDTO);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    /**
     * Función: login
     * Título: Test de inicio de sesión de usuario (Caso de Éxito)
     * Descripción: Simula una petición POST de login con credenciales válidas. 
     * Verifica que retorne status 200 (OK) y exponga correctamente el token JWT y el sessionId.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
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

    /**
     * Función: validarSesion
     * Título: Test de validación de sesión activa (Caso de Éxito)
     * Descripción: Simula una petición GET con los parámetros de sesión en la URL. 
     * Verifica que el servicio responda con status 200 (OK) y el valor booleano 'true'.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void validarSesion() throws Exception {
        when(usuarioService.isSesionValida(1L, "sesion_123")).thenReturn(true);

        mockMvc.perform(get("/api/usuarios/validar-sesion")
                        .param("id", "1")
                        .param("sessionId", "sesion_123"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /**
     * Función: eliminarUsuario
     * Título: Test de eliminación de usuario por ID (Caso de Éxito)
     * Descripción: Simula una petición DELETE a la ruta de un usuario específico. 
     * Verifica que el servicio devuelva un status 200 (OK) y el mensaje de confirmación correspondiente.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void eliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario eliminado correctamente"));
    }

    /**
     * Función: logout
     * Título: Test de cierre de sesión de usuario (Caso de Éxito)
     * Descripción: Simula una petición POST de logout enviando el identificador de sesión. 
     * Verifica que responda con status 200 (OK) y un mensaje confirmando el cierre.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void logout() throws Exception {
        doNothing().when(usuarioService).logout("sesion_123");

        mockMvc.perform(post("/api/usuarios/logout")
                        .param("sessionId", "sesion_123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Se ha cerrado sesión"));
    }

    /**
     * Función: actualizarUsuario
     * Título: Test de actualización de perfil de usuario (Caso de Éxito)
     * Descripción: Simula una petición PUT válida para modificar los datos personales de un usuario. 
     * Verifica que el endpoint retorne un status 200 (OK) confirmando la actualización exitosa.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void actualizarUsuario() throws Exception {
        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");

        when(usuarioService.actualizarUsuario(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioMockDTO);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }
}