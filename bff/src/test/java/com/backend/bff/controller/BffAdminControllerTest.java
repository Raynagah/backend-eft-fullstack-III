package com.backend.bff.controller;

import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioAdminDTO;
import com.backend.bff.dto.UsuarioDTO;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Función: BffAdminControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador de Administración (BFF)
 * Descripción: Verifica los endpoints expuestos para el panel de administración web,
 * garantizando la correcta creación, edición, listado y eliminación de usuarios.
 * Valida de forma estricta las políticas de seguridad (borrado de contraseñas de los DTOs)
 * y el manejo de excepciones de red e internas.
 */
@ExtendWith(MockitoExtension.class)
class BffAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BffUsuarioService bffUsuarioService;

    @InjectMocks
    private BffAdminController bffAdminController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bffAdminController).build();
        objectMapper = new ObjectMapper();
    }

    // ==========================================
    // TESTS PARA CREAR USUARIO POR ADMIN
    // ==========================================
    /**
     * Función: crearUsuarioPorAdmin_Exitoso_DebeRetornar200YBorrarPassword
     * Título: Validar creación de cuenta desde panel admin
     * Descripción: Comprueba que al crear un usuario con roles específicos, el controlador 
     * delegue al servicio, retorne un HTTP 200 (OK) y elimine la contraseña por seguridad.
     */
    @Test
    void crearUsuarioPorAdmin_Exitoso_DebeRetornar200YBorrarPassword() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("admin@test.com");
        request.setPassword("secreta");

        UsuarioDTO response = new UsuarioDTO();
        response.setEmail("admin@test.com");
        response.setPassword("secreta");

        when(bffUsuarioService.registrarPorAdmin(any(UsuarioDTO.class))).thenReturn(response);

        String responseBody = mockMvc.perform(post("/api/v1/web/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(responseBody.contains("secreta"), "La contraseña no debe enviarse al frontend");
        verify(bffUsuarioService, times(1)).registrarPorAdmin(any(UsuarioDTO.class));
    }

    @Test
    void crearUsuarioPorAdmin_FallaFeign_DebePropagarError() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);
        when(feignException.contentUTF8()).thenReturn("Error en los datos");

        when(bffUsuarioService.registrarPorAdmin(any(UsuarioDTO.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/web/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error en los datos"));
    }

    // ==========================================
    // TESTS PARA ACTUALIZAR USUARIO POR ADMIN
    // ==========================================
    /**
     * Función: actualizarUsuarioPorAdmin_Exitoso_DebeRetornar200YBorrarPassword
     * Título: Validar actualización de cuenta desde panel admin
     * Descripción: Verifica que la sobrescritura de datos de usuario se realice correctamente
     * retornando HTTP 200 y aplicando la limpieza de seguridad sobre la contraseña.
     */
    @Test
    void actualizarUsuarioPorAdmin_Exitoso_DebeRetornar200YBorrarPassword() throws Exception {
        UsuarioActualizacionDTO request = new UsuarioActualizacionDTO();
        request.setNombre("Admin Actualizado");

        UsuarioDTO response = new UsuarioDTO();
        response.setNombre("Admin Actualizado");
        response.setPassword("password123");

        when(bffUsuarioService.actualizarUsuarioPorAdmin(eq(1L), any(UsuarioActualizacionDTO.class))).thenReturn(response);

        String responseBody = mockMvc.perform(put("/api/v1/web/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(responseBody.contains("password123"));
        verify(bffUsuarioService, times(1)).actualizarUsuarioPorAdmin(eq(1L), any(UsuarioActualizacionDTO.class));
    }

    @Test
    void actualizarUsuarioPorAdmin_FallaFeign_DebePropagarError() throws Exception {
        UsuarioActualizacionDTO request = new UsuarioActualizacionDTO();
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(404);
        when(feignException.contentUTF8()).thenReturn("Usuario no encontrado");

        when(bffUsuarioService.actualizarUsuarioPorAdmin(eq(1L), any(UsuarioActualizacionDTO.class))).thenThrow(feignException);

        mockMvc.perform(put("/api/v1/web/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario no encontrado"));
    }

    // ==========================================
    // TESTS PARA LISTAR USUARIOS (GENERAL)
    // ==========================================
    /**
     * Función: listarUsuarios_Exitoso_DebeRetornarListaYBorrarPasswords
     * Título: Validar listado general de usuarios
     * Descripción: Comprueba que se recupere la lista de cuentas, garantizando que el bucle
     * forEach aplique la limpieza de contraseñas de manera efectiva antes de devolver un HTTP 200.
     */
    @Test
    void listarUsuarios_Exitoso_DebeRetornarListaYBorrarPasswords() throws Exception {
        List<UsuarioDTO> lista = new ArrayList<>();
        UsuarioDTO u1 = new UsuarioDTO();
        u1.setId(1L);
        u1.setPassword("clave1");
        lista.add(u1);

        when(bffUsuarioService.listarTodos()).thenReturn(lista);

        String responseBody = mockMvc.perform(get("/api/v1/web/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andReturn().getResponse().getContentAsString();

        assertFalse(responseBody.contains("clave1"));
    }

    @Test
    void listarUsuarios_FallaFeign_StatusInvalido_DebeForzar500() throws Exception {
        FeignException feignException = mock(FeignException.class);
        // Simulamos un status -1 (ej. conexión rechazada)
        when(feignException.status()).thenReturn(-1);
        when(feignException.contentUTF8()).thenReturn("Connection Refused");

        when(bffUsuarioService.listarTodos()).thenThrow(feignException);

        mockMvc.perform(get("/api/v1/web/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // El controlador lo fuerza a 500
                .andExpect(content().string("Connection Refused"));
    }

    @Test
    void listarUsuarios_ExceptionInesperada_DebeRetornar500Json() throws Exception {
        when(bffUsuarioService.listarTodos()).thenThrow(new RuntimeException("Fallo en base de datos"));

        mockMvc.perform(get("/api/v1/web/admin/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Error interno del BFF: Fallo en base de datos"));
    }

    // ==========================================
    // TESTS PARA ELIMINAR USUARIO
    // ==========================================
    /**
     * Función: eliminarUsuario_Exitoso_DebeRetornar200Json
     * Título: Validar eliminación de cuenta
     * Descripción: Verifica que el controlador capture la petición de borrado y devuelva
     * un JSON válido en texto plano para que sea fácilmente consumible por clientes como Axios.
     */
    @Test
    void eliminarUsuario_Exitoso_DebeRetornar200Json() throws Exception {
        doNothing().when(bffUsuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/v1/web/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario eliminado correctamente"));

        verify(bffUsuarioService, times(1)).eliminarUsuario(1L);
    }

    @Test
    void eliminarUsuario_FallaFeign_DebePropagarError() throws Exception {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(409);
        when(feignException.contentUTF8()).thenReturn("No se puede eliminar");

        doThrow(feignException).when(bffUsuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/v1/web/admin/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("No se puede eliminar"));
    }

    // ==========================================
    // TESTS PARA LISTAR PARA ADMIN (DTO ESPECÍFICO)
    // ==========================================
    /**
     * Función: listarParaAdmin_Exitoso_DebeRetornar200YListaAdmin
     * Título: Validar listado DTO administrador
     * Descripción: Comprueba el endpoint dedicado al dashboard del panel de control web,
     * evaluando que devuelva la colección de DTOs especializados.
     */
    @Test
    void listarParaAdmin_Exitoso_DebeRetornar200YListaAdmin() throws Exception {
        // Usamos el patrón Builder como acordamos anteriormente
        UsuarioAdminDTO admin = UsuarioAdminDTO.builder()
                .id(10L)
                .nombre("SuperAdmin")
                .correo("super@admin.com")
                .build();

        when(bffUsuarioService.listarUsuariosParaAdmin()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/v1/web/admin/usuarios/admin/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].nombre").value("SuperAdmin"));

        verify(bffUsuarioService, times(1)).listarUsuariosParaAdmin();
    }

    @Test
    void crearUsuarioPorAdmin_RespuestaNula_DebeRetornar200SinBody() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        // Simulamos el caso donde el usuario es null (rama if en controlador)
        when(bffUsuarioService.registrarPorAdmin(any(UsuarioDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/v1/web/admin/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void actualizarUsuarioPorAdmin_RespuestaNula_DebeRetornar200SinBody() throws Exception {
        UsuarioActualizacionDTO request = new UsuarioActualizacionDTO();
        // Rama if (usuarioActualizado != null)
        when(bffUsuarioService.actualizarUsuarioPorAdmin(eq(1L), any(UsuarioActualizacionDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/web/admin/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void listarUsuarios_ListaNula_NoDebeLanzarExcepcion() throws Exception {
        // Rama if(usuarios != null)
        when(bffUsuarioService.listarTodos()).thenReturn(null);

        mockMvc.perform(get("/api/v1/web/admin/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void listarUsuarios_StatusFeignInvalido_DebeRetornar500() throws Exception {
        FeignException feign = mock(FeignException.class);
        // Simulamos un status fuera de rango (ej. 600) para entrar en el if del status
        when(feign.status()).thenReturn(600);
        when(bffUsuarioService.listarTodos()).thenThrow(feign);

        mockMvc.perform(get("/api/v1/web/admin/usuarios"))
                .andExpect(status().isInternalServerError());
    }

}