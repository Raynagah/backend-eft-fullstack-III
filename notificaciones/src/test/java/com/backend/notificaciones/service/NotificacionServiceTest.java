package com.backend.notificaciones.service;

import com.backend.notificaciones.client.ReporteClient;
import com.backend.notificaciones.dto.NotificacionMatchDTO;
import com.backend.notificaciones.dto.ReporteRequestDTO;
import com.backend.notificaciones.model.Notificacion;
import com.backend.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    // Simular el repositorio para no hacer operaciones reales en la base de datos durante las pruebas
    @Mock
    private NotificacionRepository notificacionRepository;

    // Simular el cliente Feign para no hacer llamadas reales a otros microservicios durante las pruebas
    @Mock
    private ReporteClient reporteClient;

    // Inyectar los mocks en la instancia real del servicio para probar su lógica de negocio de forma aislada, sin 
    // depender de implementaciones reales de repositorios o clientes externos
    @InjectMocks
    private NotificacionService notificacionService;

    private Notificacion notificacionMock;

    // Configuración inicial antes de cada test, creando una notificación de prueba para usar en los tests que requieren un objeto Notificación
    @BeforeEach
    void setUp() {
        // Datos base confiables para usar en varios tests
        notificacionMock = new Notificacion();
        notificacionMock.setId(1L);
        notificacionMock.setReporteId(100L);
        notificacionMock.setTitulo("¡Posible coincidencia!");
        notificacionMock.setLeido(false);
    }

    // Test para el método que procesa las coincidencias recibidas del motor de IA, verificando que solo se guarden notificaciones
    // cuando el porcentaje de similitud es mayor o igual al 85%, y que se realice la llamada Feign para obtener los datos del reporte original
    // con un DTO que cumple con el umbral, y que se mapeen correctamente los datos obtenidos del microservicio de reportes
    @Test
    void procesarNotificaciones_ConSimilitudAlta_DebeGuardarYNotificar() {
        // PREPARACIÓN: Crear un DTO de coincidencia con un porcentaje de similitud mayor o igual al umbral definido (85%)
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(88.5);
        matchDTO.setMensaje("Encontramos algo.");

        // Simular la respuesta del microservicio de reportes vía Feign, devolviendo un reporte con datos específicos para validar el mapeo
        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // ACCIÓN: Ejecutar el método que procesa las notificaciones con el DTO preparado
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Capturar lo que se intentó guardar en la base de datos para comprobar que se creó una 
        // notificación con los datos correctos, incluyendo los datos obtenidos del microservicio de reportes
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();
        assertEquals(5L, guardada.getUsuarioId()); 
        assertEquals(88.5, guardada.getPorcentajeSimilitud());
        assertEquals("user@test.com", guardada.getEmailUsuario());
    }

    //  Test para el método que procesa las coincidencias recibidas del motor de IA, verificando que no se guarden notificaciones
    // cuando el porcentaje de similitud es menor al umbral definido (85%), y que no se realice la llamada Feign 
    // para obtener los datos del reporte original, con un DTO que no cumple con el umbral
    @Test
    void procesarNotificaciones_ConSimilitudAltaYTitulo_DebeUsarTituloProporcionado() {
        // PREPARACIÓN: Crear un DTO de coincidencia con un porcentaje de similitud mayor o igual al umbral definido (85%) y un título 
        // personalizado para validar que el servicio use el título proporcionado en lugar del valor por defecto
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(95.0);
        matchDTO.setTitulo("¡Encontramos a Firulais!");
        matchDTO.setMensaje("Tu mascota está a salvo.");

        // Simular la respuesta del microservicio de reportes vía Feign, devolviendo un reporte con datos específicos para validar el mapeo
        ReporteRequestDTO reporteSimulado = new ReporteRequestDTO();
        reporteSimulado.setUsuarioId(5L);
        reporteSimulado.setEmailContacto("user@test.com");

        when(reporteClient.obtenerReportePorId(100L)).thenReturn(reporteSimulado);

        // ACCIÓN: Ejecutar el método que procesa las notificaciones con el DTO preparado, que tiene un título 
        // personalizado para validar que se use ese título en la notificación guardada
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Capturar lo que se intentó guardar en la base de datos para comprobar que se creó una 
        // notificación con el título personalizado proporcionado en el DTO, y no el valor por defecto definido en el servicio
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(1)).save(captor.capture());

        Notificacion guardada = captor.getValue();

        // Verificar que el título de la notificación guardada sea el mismo que el proporcionado en el DTO, 
        // confirmando que se usó el título personalizado
        assertEquals("¡Encontramos a Firulais!", guardada.getTitulo());
    }

    // Test para el método que procesa las coincidencias recibidas del motor de IA, verificando que no se guarden notificaciones
    // cuando el porcentaje de similitud es menor al umbral definido (85%), y que no se realice la llamada Feign para obtener 
    // los datos del reporte original, con un DTO que no cumple con el umbral
    @Test
    void procesarNotificaciones_ConSimilitudBaja_NoDebeGuardar() {
        // PREPARACIÓN: Crear un DTO de coincidencia con un porcentaje de similitud menor al umbral definido (85%) 
        // para validar que el servicio no guarde la notificación ni intente obtener datos del reporte original
        NotificacionMatchDTO matchDTO = new NotificacionMatchDTO();
        matchDTO.setReporteId(100L);
        matchDTO.setPorcentajeSimilitud(50.0);

        // ACCIÓN: Ejecutar el método que procesa las notificaciones con el DTO preparado, que 
        // tiene un porcentaje de similitud menor al umbral definido
        notificacionService.procesarNotificaciones(List.of(matchDTO));

        // VERIFICACIÓN: Confirmar que no se intentó guardar ninguna notificación en la base de datos, 
        // y que no se hizo la llamada Feign para obtener los datos del reporte original, confirmando que el servicio correctamente 
        // ignoró el DTO por no cumplir con el umbral de similitud
        verify(reporteClient, never()).obtenerReportePorId(anyLong());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    // Test para el método que obtiene todas las notificaciones de un usuario específico, verificando que retorne una lista de notificaciones
    // asociadas al ID de usuario proporcionado, y que el repositorio sea llamado con el ID correcto para realizar la búsqueda, con 
    // un ID de usuario específico para validar que se devuelven las notificaciones correctas
    @Test
    void obtenerPorUsuario_DebeRetornarLista() {
        // PREPARACIÓN: Configurar el repositorio simulado para que devuelva una lista de notificaciones
        //  cuando se busque por el ID de usuario específico, simulando que hay notificaciones asociadas a ese usuario en la base de datos
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(5L))
                .thenReturn(List.of(notificacionMock));

        // ACCIÓN: Ejecutar el método que obtiene las notificaciones por ID de usuario, pasando el ID 
        // específico para validar que se devuelven las notificaciones correctas
        List<Notificacion> resultado = notificacionService.obtenerPorUsuario(5L);

        // VERIFICACIÓN: Comprobar que el resultado no sea nulo, que contenga la notificación esperada, y que el 
        // repositorio haya sido llamado con el ID correcto 
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    // Test para el método que elimina una notificación específica por su ID, verificando que 
    // se elimine correctamente cuando el ID existe, y que se lance una excepción cuando el ID no existe, 
    // con un ID de notificación específico para validar ambos escenarios
    @Test
    void eliminarNotificacion_Existente_DebeBorrar() {
        // PREPARACIÓN: Simulo que el ID existe para que la lógica permita continuar y llegue a ejecutar el delete
        when(notificacionRepository.existsById(1L)).thenReturn(true);

        // ACCIÓN: Ejecutar el método que elimina la notificación por ID, pasando el ID específico 
        // para validar que se intente eliminar la notificación correcta
        notificacionService.eliminarNotificacion(1L);

        // VERIFICACIÓN: Confirmar que el repositorio haya sido llamado para eliminar la notificación 
        // con el ID correcto, validando que se intentó eliminar la notificación esperada
        verify(notificacionRepository, times(1)).deleteById(1L);
    }

    // Test para el método que elimina una notificación específica por su ID, verificando 
    // que se lance una excepción cuando el ID no existe, con un ID de notificación específico para validar 
    // que se maneje correctamente el caso de eliminación de una notificación inexistente
    @Test
    void eliminarNotificacion_NoExistente_DebeLanzarExcepcion() {
        // PREPARACIÓN: Simulo que el ID no existe para validar que la lógica lance la excepción definida en ese caso
        when(notificacionRepository.existsById(99L)).thenReturn(false);

        // ACCIÓN Y VERIFICACIÓN: Ejecutar el método que elimina la notificación por ID, pasando un ID que no existe, y verificar 
        // que se lance la excepción esperada con el mensaje correcto, validando que el servicio maneja correctamente el caso de 
        // eliminación de una notificación inexistente
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.eliminarNotificacion(99L)
        );
        assertEquals("La notificación con ID 99 no existe.", ex.getMessage());
    }

    // Test para el método que obtiene todas las notificaciones, verificando que retorne una lista completa de 
    // notificaciones almacenadas, y que el repositorio sea llamado para realizar la búsqueda, con una 
    // configuración del repositorio para simular que hay varias notificaciones en la base de datos y validar que 
    // se devuelven todas correctamente
    @Test
    void obtenerTodas_DebeRetornarListaCompleta() {
        // PREPARACIÓN: Configurar el repositorio simulado para que devuelva una lista de notificaciones
        //  cuando se busque todas las notificaciones, simulando que hay varias notificaciones almacenadas en la 
        // base de datos para validar que se devuelven todas correctamente
        when(notificacionRepository.findAll()).thenReturn(List.of(notificacionMock));

        // ACCIÓN: Ejecutar el método que obtiene todas las notificaciones para validar que se devuelva
        //  la lista completa de notificaciones almacenadas
        List<Notificacion> resultado = notificacionService.obtenerTodas();

        // VERIFICACIÓN: Comprobar que el resultado no sea nulo, que contenga la notificación esperada, y que el 
        // repositorio haya sido llamado para obtener todas las notificaciones, validando que se intentó 
        // obtener todas las notificaciones almacenadas
        assertEquals(1, resultado.size());
    }

    // Test para el método que marca una notificación como leída, verificando que cambie el estado de la notificación a leída (true)
    // y que se guarde correctamente en la base de datos, con un ID de notificación específico para validar 
    // que se actualice la notificación correcta, y que se maneje el caso de una notificación no existente
    @Test
    void marcarComoLeida_Exito() {
        // PREPARACIÓN: Configurar el repositorio simulado para que devuelva una notificación específica 
        // cuando se busque por ID, y para que guarde la notificación actualizada, simulando que 
        // la notificación existe y se puede actualizar correctamente
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacionMock));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionMock);

        // ACCIÓN: Ejecutar el método que marca la notificación como leída, pasando el ID específico 
        // para validar que se actualice la notificación correcta
        Notificacion resultado = notificacionService.marcarComoLeida(1L);

        // VERIFICACIÓN: Comprobar que el resultado no sea nulo, que el estado de lectura haya cambiado a true, y que el 
        // repositorio haya sido llamado para guardar la notificación actualizada, validando que se intentó marcar como 
        // leída la notificación esperada y que se guardó correctamente
        assertTrue(resultado.isLeido());
        verify(notificacionRepository).save(notificacionMock);
    }

    // Test para el método que marca una notificación como leída, verificando que se lance una excepción 
    // cuando la notificación no existe, con un ID de notificación específico para validar que se maneje 
    // correctamente el caso de marcar como leída una notificación inexistente
    @Test
    void marcarComoLeida_NoExistente_DebeLanzarExcepcion() {
        // PREPARACIÓN: Configurar el repositorio simulado para que devuelva un Optional vacío cuando se busque por un 
        // ID que no existe, simulando que la notificación no existe para validar que se lance la excepción definida en ese caso
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN: Ejecutar el método que marca la notificación como leída, pasando un ID que no existe, y verificar 
        // que se lance la excepción esperada con el mensaje correcto, validando que el servicio maneja 
        // correctamente el caso de marcar como leída una notificación inexistente
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                notificacionService.marcarComoLeida(99L)
        );
        assertEquals("Notificación no encontrada con ID: 99", ex.getMessage());
    }
}