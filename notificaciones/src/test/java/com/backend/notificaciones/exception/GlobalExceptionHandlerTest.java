package com.backend.notificaciones.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    // Instanciar el manejador de excepciones para poder probar sus métodos directamente
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // Test para el método que maneja excepciones genéricas, verificando que retorne un mensaje con el prefijo definido
    // en el manejador, y que el mensaje incluya la información de la excepción original
    @Test
    void manejarGeneral_DebeRetornarErrorConPrefijo() {
        // PREPARACIÓN: Simular una excepción genérica que podría ocurrir en el servicio, como un error de conexión a la base de datos
        Exception exception = new Exception("Error de conexión a BD");

        // ACCIÓN: Procesar la excepción con el manejador y obtener el resultado
        Map<String, String> resultado = handler.manejarGeneral(exception);

        // VERIFICACIÓN: Comprobar que el resultado contenga el mensaje esperado con el prefijo definido en el manejador
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("mensaje"));
        assertEquals("Error en el servicio de notificaciones: Error de conexión a BD", resultado.get("mensaje"));
    }

    // Test para el método que maneja validaciones de DTOs, verificando que retorne un mapa con los campos y mensajes de error
    // extraídos de la excepción de validación, simulando un error común como un campo obligatorio que no fue proporcionado en el DTO
    @Test
    void manejarValidaciones_DebeMapearLosErroresDelDTO() throws NoSuchMethodException {
        // PREPARACIÓN: Simular una excepción de validación que ocurre cuando un DTO no cumple con las 
        // restricciones definidas, como un campo obligatorio que no fue proporcionado
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "notificacionMatchDTO");

        // Agregar un error de validación simulando que el campo 'porcentajeSimilitud' es obligatorio y no fue proporcionado
        bindingResult.addError(new FieldError("notificacionMatchDTO", "porcentajeSimilitud", "El porcentaje no puede ser nulo"));

        // Crear una excepción de validación utilizando el bindingResult con el error simulado, y 
        // un MethodParameter cualquiera para cumplir con la firma del constructor
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // ACCIÓN: Procesar la excepción de validación con el manejador y obtener el resultado
        Map<String, String> resultado = handler.manejarValidaciones(exception);

        // VERIFICACIÓN: Comprobar que el resultado contenga el campo con el mensaje de error esperado, y que el mapa no sea nulo
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("El porcentaje no puede ser nulo", resultado.get("porcentajeSimilitud"));
    }
}