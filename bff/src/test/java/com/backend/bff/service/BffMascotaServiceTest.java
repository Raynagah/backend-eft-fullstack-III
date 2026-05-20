package com.backend.bff.service;

import com.backend.bff.client.*;
import com.backend.bff.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BffMascotaServiceTest {

    @Mock private MascotasClient mascotaClient;
    @Mock private GeolocalizacionClient geoClient;
    @Mock private UsuarioClient usuarioClient;
    @Mock private CoincidenciasClient coincidenciasClient;
    @Mock private NotificacionClient notificacionClient;

    @InjectMocks private BffMascotaService service;

    private MascotaBaseDTO mascotaBase;

    @BeforeEach
    void setUp() {
        mascotaBase = new MascotaBaseDTO();
        mascotaBase.setId(1L);
        mascotaBase.setUsuarioId(10L);
        mascotaBase.setTipoReporte("PERDIDA");
        mascotaBase.setEspecie("Perro");
        mascotaBase.setRaza("Pug");
        mascotaBase.setColor("Negro");
        mascotaBase.setTamano("Pequeño");
        mascotaBase.setSagaStatus("COMPLETADO");
        mascotaBase.setNombre("Firulais");
        mascotaBase.setLatitud(1.0);
        mascotaBase.setLongitud(1.0);
    }

    // ==========================================
    // TESTS PARA OBTENER DASHBOARD
    // ==========================================
    @Test
    void obtenerDashboard_DebeMapearCorrectamente() {
        when(mascotaClient.obtenerTodas()).thenReturn(List.of(mascotaBase));
        List<MascotaCardDTO> res = service.obtenerDashboard();

        assertEquals(1, res.size());
        assertTrue(res.get(0).getTitulo().contains("Perro"));
    }

    // ==========================================
    // TESTS PARA DETALLE MASCOTA (RAMAS IF/CATCH)
    // ==========================================
    @Test
    void obtenerDetalleMascota_TodoExitoso() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);

        UbicacionDTO ubi = new UbicacionDTO();
        ubi.setLatitud(10.0);
        ubi.setLongitud(20.0);
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(ubi);

        UsuarioDTO user = new UsuarioDTO();
        user.setNombre("Juan");
        user.setTelefono("123456789");
        when(usuarioClient.obtenerUsuarioPorId(10L)).thenReturn(user);

        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(List.of());

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        assertEquals(10.0, res.getLatitud());
        assertEquals("Juan", res.getContactoNombre());
    }

    @Test
    void obtenerDetalleMascota_DatosNulos_NoEntraAIfs() {
        // Prueba la rama donde los clientes responden, pero con objetos nulos
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(null);
        when(usuarioClient.obtenerUsuarioPorId(any())).thenReturn(null);

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        assertNotNull(res);
        // Debe mantener los datos base de la mascota
        assertEquals(1.0, res.getLatitud());
    }

    @Test
    void obtenerDetalleMascota_AtributosNulos_FallaSegundaCondicionIf() {
        // Prueba la rama donde el objeto existe, pero latitud/nombre son nulos (&& false)
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);

        UbicacionDTO ubi = new UbicacionDTO();
        ubi.setLatitud(null); // Atributo nulo
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(ubi);

        UsuarioDTO user = new UsuarioDTO();
        user.setNombre(null); // Atributo nulo
        when(usuarioClient.obtenerUsuarioPorId(10L)).thenReturn(user);

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        assertNotNull(res);
    }

    @Test
    void obtenerDetalleMascota_ExcepcionesEnCatch() {
        // Prueba la rama donde fallan los Try y entran a los Catch
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);

        when(geoClient.obtenerUbicacionPorId(any())).thenThrow(new RuntimeException("Error Geo"));
        when(usuarioClient.obtenerUsuarioPorId(any())).thenThrow(new RuntimeException("Error User"));
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(any())).thenThrow(new RuntimeException("Error Match"));

        assertDoesNotThrow(() -> {
            MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
            assertNotNull(res);
            assertTrue(res.getPosiblesCoincidencias().isEmpty());
        });
    }

    // ==========================================
    // TESTS PARA CREAR REPORTE (RAMAS IF/CATCH)
    // ==========================================
    @Test
    void crearNuevoReporte_ConNombreValido() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setUsuarioId(10L);
        dto.setNombre("Firulais"); // Pasa las validaciones de null y blank
        dto.setEspecie("Perro");
        dto.setRaza("Pug");

        when(mascotaClient.crear(any())).thenReturn("Creado");
        doNothing().when(notificacionClient).enviarAlertaMascota(any());

        Object result = service.crearNuevoReporte(dto);
        assertEquals("Creado", result);
        verify(notificacionClient, times(1)).enviarAlertaMascota(any());
    }

    @Test
    void crearNuevoReporte_ConNombreNulo() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre(null); // Falla la validación (dto.getNombre() != null)
        dto.setEspecie("Gato");

        when(mascotaClient.crear(any())).thenReturn("Creado");

        Object result = service.crearNuevoReporte(dto);
        assertEquals("Creado", result);
    }

    @Test
    void crearNuevoReporte_ConNombreVacio_Blank() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre("   "); // Falla la validación (!dto.getNombre().isBlank())
        dto.setEspecie("Loro");

        when(mascotaClient.crear(any())).thenReturn("Creado");

        Object result = service.crearNuevoReporte(dto);
        assertEquals("Creado", result);
    }

    @Test
    void crearNuevoReporte_ExcepcionEnNotificacion() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre("Rex");
        dto.setEspecie("Perro");

        when(mascotaClient.crear(any())).thenReturn("Creado");
        // Forzamos a que entre al bloque Catch de notificaciones
        doThrow(new RuntimeException("Falla Notificación")).when(notificacionClient).enviarAlertaMascota(any());

        assertDoesNotThrow(() -> {
            Object result = service.crearNuevoReporte(dto);
            assertEquals("Creado", result); // El catch se traga el error y retorna la respuesta de crear
        });
    }
}