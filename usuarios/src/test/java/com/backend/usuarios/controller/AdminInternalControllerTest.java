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

import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Función: AdminInternalControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador Interno de Administración
 * Descripción: Verifica el correcto funcionamiento de los endpoints expuestos para la 
 * gestión interna de usuarios, validando que el controlador responda con los códigos HTTP 
 * adecuados y los objetos DTO esperados al interactuar con el servicio mockeado.
 */
@ExtendWith(MockitoExtension.class)
class AdminInternalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AdminInternalController adminInternalController;

    private ObjectMapper objectMapper;
    private UsuarioDTO usuarioMockAdminDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminInternalController).build();
        objectMapper = new ObjectMapper();

        // Instanciamos el DTO de respuesta mockeado
        usuarioMockAdminDTO = new UsuarioDTO(
                5L,
                "Nuevo Registro Admin",
                "555444333",
                "nuevoadmin@test.com",
                30,
                "M",
                "HQ",
                "CEO",
                "url",
                "admin"
        );
    }

    /**
     * Función: crearPorAdmin_Exitoso
     * Título: Test de creación de usuario con rol a elección (Caso de Éxito)
     * Descripción: Simula una petición POST válida para crear un usuario administrador. 
     * Verifica que el endpoint retorne un status 201 (CREATED) y que el JSON de respuesta 
     * contenga correctamente el rol y nombre asignados.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void crearPorAdmin_Exitoso() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("Nuevo Registro Admin", 30, "M", "nuevoadmin@test.com", "123456", "555444333", "url", "CEO", "HQ", "admin");

        // Le decimos a Mockito que devuelva el DTO simulado cuando el servicio sea llamado
        when(usuarioService.crearUsuarioAdmin(any(UsuarioRequestDTO.class))).thenReturn(usuarioMockAdminDTO);

        mockMvc.perform(post("/internal/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoUsuario").value("admin"))
                .andExpect(jsonPath("$.nombre").value("Nuevo Registro Admin"));
    }

    /**
     * Función: actualizarPorAdmin_Exitoso
     * Título: Test de actualización total de usuario incluyendo rol (Caso de Éxito)
     * Descripción: Simula una petición PUT válida para modificar integralmente el perfil de 
     * un usuario existente. Verifica que el endpoint retorne un status 200 (OK) y que los 
     * datos del usuario (como el rol) reflejen el comportamiento esperado.
     *
     * @throws Exception Si ocurre un error al ejecutar la petición con MockMvc.
     */
    @Test
    void actualizarPorAdmin_Exitoso() throws Exception {
        UsuarioUpdateDTO update = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "admin");

        // Le decimos a Mockito que devuelva el DTO simulado cuando el servicio sea llamado para el ID 1
        when(usuarioService.actualizarUsuarioPorAdmin(eq(1L), any(UsuarioUpdateDTO.class))).thenReturn(usuarioMockAdminDTO);

        mockMvc.perform(put("/internal/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoUsuario").value("admin"));
    }
}