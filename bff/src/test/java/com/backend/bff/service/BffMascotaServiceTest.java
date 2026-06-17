package com.backend.bff.service;

import com.backend.bff.client.*;
import com.backend.bff.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Función: BffMascotaServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Orquestador de Mascotas (BFF)
 * Descripción: Verifica que el servicio consolide correctamente la información 
 * proveniente de distintos microservicios (Mascotas, Geo, Usuarios, Coincidencias).
 * Asegura el manejo tolerante a fallos (try-catch), la cobertura total de ramas lógicas
 * y la correcta extracción de identificadores al crear nuevos reportes.
 */
@ExtendWith(MockitoExtension.class)
class BffMascotaServiceTest {

    @Mock private MascotasClient mascotaClient;
    @Mock private GeolocalizacionClient geoClient;
    @Mock private UsuarioClient usuarioClient;
    @Mock private CoincidenciasClient coincidenciasClient;

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
    void obtenerDetalleMascota_TodoExitoso_ConEnriquecimientoCoincidencias() {
        // 1. Mascota Base
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);

        // 2. Geo
        UbicacionDTO ubi = new UbicacionDTO();
        ubi.setLatitud(10.0);
        ubi.setLongitud(20.0);
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(ubi);

        // 3. Usuario
        UsuarioDTO user = new UsuarioDTO();
        user.setNombre("Juan");
        user.setTelefono("123456789");
        when(usuarioClient.obtenerUsuarioPorId(10L)).thenReturn(user);

        // 4. Coincidencias
        CoincidenciaDTO coincidencia = new CoincidenciaDTO();
        coincidencia.setMascotaId(2L); // El ID del match
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(List.of(coincidencia));
        
        // 5. Enriquecimiento dentro del loop de coincidencias
        MascotaBaseDTO mascotaMatch = new MascotaBaseDTO();
        mascotaMatch.setTipoReporte("ENCONTRADA");
        when(mascotaClient.obtenerPorId(2L)).thenReturn(mascotaMatch);

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        
        assertEquals(10.0, res.getLatitud());
        assertEquals("Juan", res.getContactoNombre());
        assertEquals(1, res.getPosiblesCoincidencias().size());
        assertEquals("ENCONTRADA", res.getPosiblesCoincidencias().get(0).getTipoReporte());
    }

    @Test
    void obtenerDetalleMascota_DatosNulos_NoEntraAIfs() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(null);
        when(usuarioClient.obtenerUsuarioPorId(any())).thenReturn(null);
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(null);

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        assertNotNull(res);
        assertEquals(1.0, res.getLatitud()); // Mantiene el original
    }

    /**
     * Función: obtenerDetalleMascota_AtributosEspecificosNulosYCoincidenciasVacias
     * Título: Cubrir ramas con objetos no nulos pero propiedades internas nulas
     * Descripción: Evalúa las ramas "&&" donde el objeto existe pero su atributo clave 
     * (latitud, nombre) es nulo, y cuando la lista de coincidencias existe pero está vacía.
     */
    @Test
    void obtenerDetalleMascota_AtributosEspecificosNulosYCoincidenciasVacias() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        
        // Ubicación no nula, pero latitud nula
        UbicacionDTO ubiVacia = new UbicacionDTO();
        when(geoClient.obtenerUbicacionPorId(1L)).thenReturn(ubiVacia);
        
        // Usuario no nulo, pero nombre nulo
        UsuarioDTO userVacio = new UsuarioDTO();
        when(usuarioClient.obtenerUsuarioPorId(10L)).thenReturn(userVacio);
        
        // Lista no nula, pero vacía
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(new ArrayList<>());

        MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
        
        assertEquals(1.0, res.getLatitud()); // Mantuvo el original
        assertNull(res.getContactoNombre()); // Mantuvo el original (que era null en mascotaBase por defecto al inicializar o mapear)
        assertTrue(res.getPosiblesCoincidencias().isEmpty());
    }

    @Test
    void obtenerDetalleMascota_ExcepcionesEnCatchPrincipales() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        when(geoClient.obtenerUbicacionPorId(any())).thenThrow(new RuntimeException("Error Geo"));
        when(usuarioClient.obtenerUsuarioPorId(any())).thenThrow(new RuntimeException("Error User"));
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(any())).thenThrow(new RuntimeException("Error Match"));

        assertDoesNotThrow(() -> {
            MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
            assertNotNull(res);
            assertTrue(res.getPosiblesCoincidencias().isEmpty()); // El catch setea ArrayList vacío
        });
    }

    /**
     * Función: obtenerDetalleMascota_MascotaMatchNula
     * Título: Cubrir rama donde el registro enlazado no existe
     * Descripción: Evalúa la rama del if(datosMascotaMatch != null) cuando el microservicio 
     * no encuentra el match de la coincidencia y retorna nulo.
     */
    @Test
    void obtenerDetalleMascota_MascotaMatchNula() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        
        CoincidenciaDTO coincidencia = new CoincidenciaDTO();
        coincidencia.setMascotaId(55L); 
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(List.of(coincidencia));
        
        // Simulamos que devuelve nulo al buscar la mascota del match
        when(mascotaClient.obtenerPorId(55L)).thenReturn(null);

        assertDoesNotThrow(() -> {
            MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
            assertEquals(1, res.getPosiblesCoincidencias().size());
            assertNull(res.getPosiblesCoincidencias().get(0).getTipoReporte());
        });
    }

    @Test
    void obtenerDetalleMascota_FallaEnriquecimientoDeCoincidencia_ContinuaEjecucion() {
        when(mascotaClient.obtenerPorId(1L)).thenReturn(mascotaBase);
        
        CoincidenciaDTO coincidencia = new CoincidenciaDTO();
        coincidencia.setMascotaId(99L); 
        when(coincidenciasClient.obtenerCoincidenciasPorMascota(1L)).thenReturn(List.of(coincidencia));
        
        // Simulamos que al tratar de enriquecer esta coincidencia específica, el ms-mascotas falla
        when(mascotaClient.obtenerPorId(99L)).thenThrow(new RuntimeException("Mascota match no existe"));

        assertDoesNotThrow(() -> {
            MascotaDetalleCompletoDTO res = service.obtenerDetalleMascota(1L);
            assertEquals(1, res.getPosiblesCoincidencias().size());
            assertNull(res.getPosiblesCoincidencias().get(0).getTipoReporte()); // No se pudo enriquecer
        });
    }

    // ==========================================
    // TESTS PARA CREAR REPORTE (RAMAS IF/CATCH)
    // ==========================================

    @Test
    void crearNuevoReporte_ConNombreValidoYExtraccionMap_DebeLlamarCoincidencias() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setUsuarioId(10L);
        dto.setNombre("Firulais"); 
        dto.setEspecie("Perro");
        dto.setRaza("Pug");

        // Simulamos que Feign nos devuelve un Map con el ID de la mascota creada
        Map<String, Object> mockResponse = Map.of("id", 150);
        when(mascotaClient.crear(any())).thenReturn(mockResponse);

        Object result = service.crearNuevoReporte(dto);
        
        assertEquals(mockResponse, result);
        // Verificamos que se haya procesado la coincidencia con el ID extraído (150L)
        verify(coincidenciasClient, times(1)).procesarCoincidencias(150L);
    }

    @Test
    void crearNuevoReporte_NoEsInstanciaDeMap_NoDebeLlamarCoincidencias() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre(null); // Entra al "Sin nombre"
        
        // Simulamos una respuesta que NO es Map (ej. String)
        when(mascotaClient.crear(any())).thenReturn("Respuesta Plana");

        Object result = service.crearNuevoReporte(dto);
        assertEquals("Respuesta Plana", result);
        
        // Al no poder extraer el ID, nunca debería llamar a procesarCoincidencias
        verify(coincidenciasClient, never()).procesarCoincidencias(anyLong());
    }

    /**
     * Función: crearNuevoReporte_NombreEnBlanco_MapSinIdLanzaExcepcion
     * Título: Cubrir rama isBlank y fallo en extracción (catch)
     * Descripción: Fuerza el operador isBlank() a verdadero enviando espacios, y 
     * desencadena el NullPointerException interno en la extracción simulando un mapa 
     * válido pero carente de la clave "id", evaluando la rama del catch de conversión.
     */
    @Test
    void crearNuevoReporte_NombreEnBlanco_MapSinIdLanzaExcepcion() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre("   "); // isBlank() == true

        // Simulamos un mapa, pero SIN la clave "id". Esto hará que get("id") sea null,
        // provocando un NPE al hacer .toString(), activando el catch de extracción.
        Map<String, Object> mockResponse = new java.util.HashMap<>();
        mockResponse.put("otraClave", "valor");
        
        when(mascotaClient.crear(any())).thenReturn(mockResponse);

        Object result = service.crearNuevoReporte(dto);
        
        assertEquals(mockResponse, result);
        verify(coincidenciasClient, never()).procesarCoincidencias(anyLong());
    }

    @Test
    void crearNuevoReporte_ExcepcionAlProcesarCoincidencias() {
        WebReporteRequestDTO dto = new WebReporteRequestDTO();
        dto.setNombre("Rex");
        
        Map<String, Object> mockResponse = Map.of("id", 200);
        when(mascotaClient.crear(any())).thenReturn(mockResponse);
        
        // Forzamos el catch en coincidenciasClient
        doThrow(new RuntimeException("Fallo en motor")).when(coincidenciasClient).procesarCoincidencias(200L);

        assertDoesNotThrow(() -> {
            Object result = service.crearNuevoReporte(dto);
            assertEquals(mockResponse, result); // No debe romper la respuesta al frontend
        });
    }

    // ==========================================
    // TESTS PARA ELIMINAR REPORTE
    // ==========================================

    @Test
    void eliminarReporte_DebeLlamarAlCliente() {
        Long idMascota = 10L;
        
        // Se ejecuta el método void
        service.eliminarReporte(idMascota);
        
        // Verificamos que se delegue correctamente
        verify(mascotaClient, times(1)).eliminar(idMascota);
    }
}