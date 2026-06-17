package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioAdminDTO;
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

/**
 * Función: BffUsuarioControllerTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Controlador Web de Usuarios (BFF)
 * Descripción: Verifica los endpoints encargados de la gestión de perfiles de usuario, 
 * historial de reportes, registro público de nuevas cuentas y la generación de 
 * listados administrativos, garantizando una correcta propagación de errores mediante Feign.
 */
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

    /**
     * Función: obtenerPerfil_DebeRetornarUsuarioYStatus200
     * Título: Validar obtención de perfil de usuario
     * Descripción: Comprueba que el controlador responda correctamente a la petición GET, 
     * delegando la búsqueda al servicio y retornando la información personal del usuario 
     * junto con un código HTTP 200 (OK).
     */
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

    /**
     * Función: actualizarPerfil_DebeRetornarUsuarioYStatus200
     * Título: Validar actualización de perfil
     * Descripción: Verifica que se reciba el payload con la información a actualizar y el ID 
     * en la ruta (path variable), ejecutando el mapeo adecuado hacia el servicio y retornando 
     * el usuario modificado con estado 200 (OK).
     */
    @Test
    void actualizarPerfil_DebeRetornarUsuarioYStatus200() throws Exception {
        UsuarioActualizacionDTO updateDto = new UsuarioActualizacionDTO();
        updateDto.setNombre("Carlos Actualizado");

        UsuarioDTO updatedUser = new UsuarioDTO();
        updatedUser.setId(1L);
        updatedUser.setNombre("Carlos Actualizado");

        when(bffUsuarioService.actualizarUsuario(eq(1L), any(UsuarioActualizacionDTO.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/web/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos Actualizado"));

        verify(bffUsuarioService, times(1)).actualizarUsuario(eq(1L), any(UsuarioActualizacionDTO.class));
    }

    /**
     * Función: obtenerMisReportes_DebeRetornarListaYStatus200
     * Título: Validar obtención del historial de reportes
     * Descripción: Asegura que el controlador recupere correctamente las tarjetas (cards) 
     * de mascotas vinculadas al ID de un usuario específico, retornando la lista 
     * en formato JSON con estado HTTP 200 (OK).
     */
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

    /**
     * Función: registro_Exitoso_DebeRetornar200
     * Título: Validar flujo de registro exitoso
     * Descripción: Comprueba que ante un intento de creación de cuenta con datos válidos, 
     * el controlador delegue al servicio de autenticación y devuelva los datos de confirmación 
     * del nuevo usuario.
     */
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

    /**
     * Función: registro_FallaMicroservicio_DebeRetornarErrorDeFeign
     * Título: Validar propagación de error en registro
     * Descripción: Verifica que, si ocurre un conflicto en el microservicio subyacente 
     * (ej. correo duplicado con HTTP 409), el BFF intercepte la FeignException y refleje 
     * exactamente el mismo error al frontend.
     */
    @Test
    void registro_FallaMicroservicio_DebeRetornarErrorDeFeign() throws Exception {
        UsuarioDTO request = new UsuarioDTO();
        request.setEmail("duplicado@test.com");

        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(409);
        when(feignException.contentUTF8()).thenReturn("El correo ya está registrado");

        when(authService.registrar(any(UsuarioDTO.class))).thenThrow(feignException);

        mockMvc.perform(post("/api/v1/web/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) 
                .andExpect(content().string("El correo ya está registrado"));
    }

    /**
     * Función: listarParaAdmin_DebeRetornarListaYStatus200
     * Título: Validar listado de usuarios para administración
     * Descripción: Comprueba que el controlador recupere y sirva correctamente una lista 
     * de objetos DTO optimizados para la vista de administrador de la plataforma web.
     */
    @Test
    void listarParaAdmin_DebeRetornarListaYStatus200() throws Exception {
        UsuarioAdminDTO adminUser = UsuarioAdminDTO.builder()
                .id(100L)
                .nombre("Admin Tester")
                .correo("admin@test.com") 
                .build();

        when(bffUsuarioService.listarUsuariosParaAdmin()).thenReturn(List.of(adminUser));

        mockMvc.perform(get("/api/v1/web/usuarios/admin/listar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].correo").value("admin@test.com")); 

        verify(bffUsuarioService, times(1)).listarUsuariosParaAdmin();
    }
}