package com.backend.ms_geolocalizacion.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.ms_geolocalizacion.exception.BadRequestException;
import com.backend.ms_geolocalizacion.exception.ResourceNotFoundException;
import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;

/**
 * Función: GeoServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Geolocalización
 * Descripción: Valida la lógica de negocio para la gestión de ubicaciones espaciales. 
 * Incluye pruebas exhaustivas sobre la validación de límites geográficos (latitud/longitud), 
 * el filtrado por proximidad espacial y el manejo adecuado de excepciones.
 */
@ExtendWith(MockitoExtension.class)
class GeoServiceTest {

    @Mock
    private UbicacionAlertaRepository repository;

    @InjectMocks
    private GeoService geoService;

    private UbicacionAlerta ubicacion;

    @BeforeEach
    void setUp() {
        ubicacion = new UbicacionAlerta();
        ubicacion.setId(1L);
        ubicacion.setReporteId(100L);
        ubicacion.setLatitud(-33.4489); 
        ubicacion.setLongitud(-70.6693);
    }

    /**
     * Función: registrarUbicacion_DebeGuardarYRetornarObjeto
     * Título: Validar persistencia exitosa
     * Descripción: Comprueba que al enviar un objeto válido con coordenadas correctas, 
     * el servicio lo persista en el repositorio y retorne la entidad generada.
     */
    @Test
    void registrarUbicacion_DebeGuardarYRetornarObjeto() {
        when(repository.save(any(UbicacionAlerta.class))).thenReturn(ubicacion);

        UbicacionAlerta resultado = geoService.registrarUbicacion(ubicacion);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repository, times(1)).save(ubicacion);
    }

    /**
     * Función: registrarUbicacion_CuandoReporteIdEsNull_DebeLanzarBadRequestException
     * Título: Rechazar registro sin ID de reporte
     * Descripción: Asegura que el servicio lance una BadRequestException y bloquee la 
     * persistencia si el objeto carece del identificador de reporte asociado.
     */
    @Test
    void registrarUbicacion_CuandoReporteIdEsNull_DebeLanzarBadRequestException() {
        ubicacion.setReporteId(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertEquals("El ID de reporte es obligatorio.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    /**
     * Función: registrarUbicacion_CuandoLatitudInvalida_DebeLanzarBadRequestException
     * Título: Rechazar latitud fuera de límites matemáticos
     * Descripción: Valida el límite superior de la latitud, comprobando que valores mayores 
     * a 90.0 arrojen una excepción de validación.
     */
    @Test
    void registrarUbicacion_CuandoLatitudInvalida_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(95.0); 

        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertEquals("La latitud debe ser un valor numérico válido entre -90.0 y 90.0.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    /**
     * Función: obtenerTodas_DebeRetornarListaDeUbicaciones
     * Título: Recuperar listado global
     * Descripción: Verifica que la consulta delegue exitosamente al repositorio y 
     * retorne una lista de ubicaciones.
     */
    @Test
    void obtenerTodas_DebeRetornarListaDeUbicaciones() {
        when(repository.findAll()).thenReturn(List.of(ubicacion));

        List<UbicacionAlerta> resultados = geoService.obtenerTodas();

        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
    }

    /**
     * Función: buscarCercanas_DebeFiltrarPorRadioCorrectamente
     * Título: Filtrar ubicaciones por proximidad
     * Descripción: Valida que la fórmula matemática (Haversine) filtre con precisión las ubicaciones 
     * que están dentro de un radio de distancia en kilómetros, excluyendo las lejanas.
     */
    @Test
    void buscarCercanas_DebeFiltrarPorRadioCorrectamente() {
        UbicacionAlerta cercana = new UbicacionAlerta();
        cercana.setLatitud(0.01); 
        cercana.setLongitud(0.01);

        UbicacionAlerta lejana = new UbicacionAlerta();
        lejana.setLatitud(5.0); 
        lejana.setLongitud(5.0);

        when(repository.findAll()).thenReturn(List.of(cercana, lejana));

        List<UbicacionAlerta> resultados = geoService.buscarCercanas(0.0, 0.0, 50.0);

        assertEquals(1, resultados.size());
        assertEquals(0.01, resultados.get(0).getLatitud());
    }

    /**
     * Función: buscarCercanas_CuandoRadioEsInvalido_DebeLanzarBadRequestException
     * Título: Rechazar búsqueda con radio negativo
     * Descripción: Asegura que una búsqueda espacial con un radio inferior a cero no se ejecute.
     */
    @Test
    void buscarCercanas_CuandoRadioEsInvalido_DebeLanzarBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(0.0, 0.0, -10.0)
        );
        
        assertEquals("El radio de búsqueda debe ser mayor a 0 KM.", exception.getMessage());
    }

    /**
     * Función: eliminarUbicacion_CuandoExiste_DebeEliminarSinErrores
     * Título: Validar flujo de eliminación
     * Descripción: Comprueba que al eliminar una ubicación existente, el servicio la busque, 
     * la encuentre en el repositorio y proceda a su eliminación física sin lanzar errores.
     */
    @Test
    void eliminarUbicacion_CuandoExiste_DebeEliminarSinErrores() {
        when(repository.findById(1L)).thenReturn(Optional.of(ubicacion));
        doNothing().when(repository).delete(ubicacion);

        assertDoesNotThrow(() -> geoService.eliminarUbicacion(1L));
        verify(repository, times(1)).delete(ubicacion);
    }

    /**
     * Función: eliminarUbicacion_CuandoNoExiste_DebeLanzarResourceNotFoundException
     * Título: Propagar error si la ubicación no existe
     * Descripción: Verifica que un intento de eliminar un ID inexistente dispare la excepción 
     * NotFound y detenga la operación de borrado.
     */
    @Test
    void eliminarUbicacion_CuandoNoExiste_DebeLanzarResourceNotFoundException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoService.eliminarUbicacion(2L)
        );
        
        assertEquals("No se encontró ninguna ubicación con el ID: 2", exception.getMessage());
        verify(repository, never()).delete(any());
    }

    /**
     * Función: obtenerPorReporteId_CuandoExiste_DebeRetornarObjeto
     * Título: Búsqueda exitosa por reporte vinculado
     * Descripción: Garantiza la correcta localización de una ubicación asociada a un ID de reporte.
     */
    @Test
    void obtenerPorReporteId_CuandoExiste_DebeRetornarObjeto() {
        when(repository.findByReporteId(100L)).thenReturn(Optional.of(ubicacion));

        UbicacionAlerta resultado = geoService.obtenerPorReporteId(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getReporteId());
    }

    /**
     * Función: obtenerPorReporteId_CuandoNoExiste_DebeLanzarResourceNotFoundException
     * Título: Fallo en búsqueda por reporte vinculado
     * Descripción: Valida que buscar un ID de reporte que no tenga ubicaciones registradas lance NotFound.
     */
    @Test
    void obtenerPorReporteId_CuandoNoExiste_DebeLanzarResourceNotFoundException() {
        when(repository.findByReporteId(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> geoService.obtenerPorReporteId(999L)
        );
        
        assertEquals("No se encontró ninguna ubicación asociada al reporte con ID: 999", exception.getMessage());
    }

    /**
     * Función: registrarUbicacion_CuandoLatitudEsNull_DebeLanzarBadRequestException
     * Título: Rechazar registro sin latitud
     * Descripción: Comprueba la barrera de seguridad contra atributos de latitud nulos.
     */
    @Test
    void registrarUbicacion_CuandoLatitudEsNull_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(null);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception); 
        verify(repository, never()).save(any());
    }

    /**
     * Función: registrarUbicacion_CuandoLatitudMenorAMenos90_DebeLanzarBadRequestException
     * Título: Rechazar límite inferior de latitud
     * Descripción: Valida que valores menores a -90.0 no sean admitidos por la lógica de coordenadas.
     */
    @Test
    void registrarUbicacion_CuandoLatitudMenorAMenos90_DebeLanzarBadRequestException() {
        ubicacion.setLatitud(-91.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    /**
     * Función: registrarUbicacion_CuandoLongitudEsNull_DebeLanzarBadRequestException
     * Título: Rechazar registro sin longitud
     * Descripción: Comprueba la barrera de seguridad contra atributos de longitud nulos.
     */
    @Test
    void registrarUbicacion_CuandoLongitudEsNull_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(null);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    /**
     * Función: registrarUbicacion_CuandoLongitudMenorAMenos180_DebeLanzarBadRequestException
     * Título: Rechazar límite inferior de longitud
     * Descripción: Valida que valores menores a -180.0 disparen un BadRequestException.
     */
    @Test
    void registrarUbicacion_CuandoLongitudMenorAMenos180_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(-181.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    /**
     * Función: registrarUbicacion_CuandoLongitudMayorA180_DebeLanzarBadRequestException
     * Título: Rechazar límite superior de longitud
     * Descripción: Valida que valores mayores a 180.0 disparen un BadRequestException.
     */
    @Test
    void registrarUbicacion_CuandoLongitudMayorA180_DebeLanzarBadRequestException() {
        ubicacion.setLongitud(181.0);
        
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.registrarUbicacion(ubicacion)
        );
        
        assertNotNull(exception);
        verify(repository, never()).save(any());
    }

    /**
     * Función: buscarCercanas_CuandoRadioEsCero_DebeLanzarBadRequestException
     * Título: Rechazar radio equivalente a cero
     * Descripción: Garantiza que el radio de búsqueda espacial siempre represente una magnitud positiva real.
     */
    @Test
    void buscarCercanas_CuandoRadioEsCero_DebeLanzarBadRequestException() {
        BadRequestException exception = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(-33.0, -70.0, 0.0)
        );
        
        assertNotNull(exception);
    }

    /**
     * Función: buscarCercanas_CuandoLatitudOlongitudInvalida_DebeLanzarBadRequestException
     * Título: Fallo en coordenadas origen para búsqueda
     * Descripción: Verifica que se exijan coordenadas de origen sólidas antes de iniciar una búsqueda por proximidad.
     */
    @Test
    void buscarCercanas_CuandoLatitudOlongitudInvalida_DebeLanzarBadRequestException() {
        BadRequestException exceptionLat = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(null, -70.0, 50.0)
        );
        
        BadRequestException exceptionLon = assertThrows(
                BadRequestException.class, 
                () -> geoService.buscarCercanas(-33.0, null, 50.0)
        );
        
        assertNotNull(exceptionLat);
        assertNotNull(exceptionLon);
    }
}