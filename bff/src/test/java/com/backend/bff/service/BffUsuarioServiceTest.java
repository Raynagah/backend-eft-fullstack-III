package com.backend.bff.service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaBaseDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.dto.UsuarioAdminDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Función: BffUsuarioServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Usuarios (BFF)
 * Descripción: Verifica que la orquestación de usuarios funcione correctamente,
 * incluyendo consultas básicas, filtrado de reportes por usuario, y las 
 * integraciones más complejas del panel de administración (cruce de métricas).
 */
@ExtendWith(MockitoExtension.class)
class BffUsuarioServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private MascotasClient mascotaClient;

    @InjectMocks
    private BffUsuarioService service;

    // ==========================================
    // TESTS DE USUARIO ESTÁNDAR
    // ==========================================

    @Test
    void obtenerUsuario_DebeRetornarObjeto() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.obtenerUsuarioPorId(1L)).thenReturn(dto);
        
        assertNotNull(service.obtenerUsuario(1L));
        verify(usuarioClient, times(1)).obtenerUsuarioPorId(1L);
    }

    @Test
    void actualizarUsuario_DebeRetornarObjeto() {
        UsuarioActualizacionDTO act = new UsuarioActualizacionDTO();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.actualizarUsuario(1L, act)).thenReturn(dto);
        
        assertNotNull(service.actualizarUsuario(1L, act));
    }

    @Test
    void obtenerReportesPorUsuario_DebeFiltrarCorrectamenteYMapearFechas() {
        // Mascota 1: Coincide con el usuario buscado, tiene fecha como String
        MascotaBaseDTO m1 = new MascotaBaseDTO();
        m1.setId(100L);
        m1.setUsuarioId(1L);
        m1.setTipoReporte("PERDIDA");
        m1.setEspecie("Perro");
        m1.setRaza("Pug");
        m1.setFechaReporte("2026-06-16T22:00:00"); // Corregido: Pasamos un String válido

        // Mascota 2: Pertenece a otro usuario (no coincide)
        MascotaBaseDTO m2 = new MascotaBaseDTO();
        m2.setId(101L);
        m2.setUsuarioId(2L);

        // Mascota 3: Coincide pero NO tiene fecha (cubre fechaReporte nulo en el ternario)
        MascotaBaseDTO m3 = new MascotaBaseDTO();
        m3.setId(102L);
        m3.setUsuarioId(1L);
        m3.setFechaReporte(null); 

        // Mascota 4: Caso extremo con usuarioId NULL
        MascotaBaseDTO m4 = new MascotaBaseDTO();
        m4.setId(103L);
        m4.setUsuarioId(null);

        when(mascotaClient.obtenerTodas()).thenReturn(List.of(m1, m2, m3, m4));

        List<MascotaCardDTO> resultados = service.obtenerReportesPorUsuario(1L);

        assertEquals(2, resultados.size()); // Debería traer m1 y m3
        assertEquals(100L, resultados.get(0).getId());
        assertEquals("2026-06-16T22:00:00", resultados.get(0).getFechaReporte()); // Verifica el mapeo del String
        assertEquals(102L, resultados.get(1).getId());
        assertNull(resultados.get(1).getFechaReporte()); // Asegura que el mapeo null funcione
    }

    @Test
    void registrar_DebeRetornarObjeto() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.registrar(dto)).thenReturn(dto);
        assertNotNull(service.registrar(dto));
    }

    // ==========================================
    // TESTS DE ADMINISTRADOR
    // ==========================================

    @Test
    void registrarPorAdmin_DebeRetornarObjeto() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.registrarAdmin(dto)).thenReturn(dto);
        assertNotNull(service.registrarPorAdmin(dto));
    }

    @Test
    void actualizarUsuarioPorAdmin_DebeRetornarObjeto() {
        UsuarioActualizacionDTO act = new UsuarioActualizacionDTO();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.actualizarUsuarioPorAdmin(1L, act)).thenReturn(dto);
        
        assertNotNull(service.actualizarUsuarioPorAdmin(1L, act));
    }

    @Test
    void listarTodos_DebeRetornarLista() {
        when(usuarioClient.listarUsuarios()).thenReturn(List.of(new UsuarioDTO()));
        
        List<UsuarioDTO> resultados = service.listarTodos();
        assertFalse(resultados.isEmpty());
        verify(usuarioClient, times(1)).listarUsuarios();
    }

    @Test
    void eliminarUsuario_DebeLlamarAlCliente() {
        service.eliminarUsuario(1L);
        verify(usuarioClient, times(1)).eliminarUsuario(1L);
    }

    @Test
    void listarUsuariosParaAdmin_FlujoExitoso_DebeContarMascotas() {
        // 1. Preparamos 2 usuarios
        UsuarioDTO u1 = new UsuarioDTO();
        u1.setId(10L);
        u1.setNombre("Carlos");

        UsuarioDTO u2 = new UsuarioDTO();
        u2.setId(20L);
        u2.setNombre("Ana");

        when(usuarioClient.listarUsuarios()).thenReturn(List.of(u1, u2));

        // 2. Preparamos mascotas (Carlos tiene 2, Ana tiene 0)
        MascotaBaseDTO m1 = new MascotaBaseDTO(); m1.setUsuarioId(10L);
        MascotaBaseDTO m2 = new MascotaBaseDTO(); m2.setUsuarioId(10L);
        MascotaBaseDTO m3 = new MascotaBaseDTO(); m3.setUsuarioId(null); // Ignorada
        
        when(mascotaClient.obtenerTodas()).thenReturn(List.of(m1, m2, m3));

        // 3. Ejecutamos
        List<UsuarioAdminDTO> resultados = service.listarUsuariosParaAdmin();

        // 4. Verificamos métricas
        assertEquals(2, resultados.size());
        assertEquals("Carlos", resultados.get(0).getNombre());
        assertEquals(2, resultados.get(0).getCantidadReportes()); // Carlos tiene 2 mascotas
        
        assertEquals("Ana", resultados.get(1).getNombre());
        assertEquals(0, resultados.get(1).getCantidadReportes()); // Ana tiene 0 mascotas
    }

    @Test
    void listarUsuariosParaAdmin_ExcepcionEnMascotas_DebeRetornarCeros() {
        // Configuramos el usuario
        UsuarioDTO u1 = new UsuarioDTO();
        u1.setId(10L);
        when(usuarioClient.listarUsuarios()).thenReturn(List.of(u1));

        // Forzamos un error al intentar traer las mascotas
        when(mascotaClient.obtenerTodas()).thenThrow(new RuntimeException("Fallo en ms-mascotas"));

        // Aseguramos que la excepción es capturada y no interrumpe el flujo
        assertDoesNotThrow(() -> {
            List<UsuarioAdminDTO> resultados = service.listarUsuariosParaAdmin();
            assertEquals(1, resultados.size());
            assertEquals(0, resultados.get(0).getCantidadReportes()); // El contador queda en 0
        });
    }
}