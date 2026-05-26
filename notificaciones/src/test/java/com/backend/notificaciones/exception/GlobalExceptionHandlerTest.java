package com.backend.notificaciones.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    // Instancio mi clase manejadora de errores real
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarGeneral_DebeRetornarErrorConPrefijo() {
        // PREPARACIÓN: Simulo una falla catastrófica en el sistema
        Exception exception = new Exception("Error de conexión a BD");

        // ACCIÓN: Le paso la excepción a mi manejador
        Map<String, String> resultado = handler.manejarGeneral(exception);

        // VERIFICACIÓN: Valido que mi manejador haya empaquetado el error con mi prefijo personalizado
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("mensaje"));
        assertEquals("Error en el servicio de notificaciones: Error de conexión a BD", resultado.get("mensaje"));
    }

    @Test
    void manejarValidaciones_DebeMapearLosErroresDelDTO() throws NoSuchMethodException {
        // PREPARACIÓN: Simulo un DTO que falló la validación de Spring (@Valid)
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "notificacionMatchDTO");

        // Agrego un error simulando que alguien envió un porcentaje nulo
        bindingResult.addError(new FieldError("notificacionMatchDTO", "porcentajeSimilitud", "El porcentaje no puede ser nulo"));

        // Armo la excepción de validación que usa Spring por debajo
        MethodParameter parameter = new MethodParameter(Object.class.getMethod("toString"), -1);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // ACCIÓN: Proceso el error
        Map<String, String> resultado = handler.manejarValidaciones(exception);

        // VERIFICACIÓN: Compruebo que haya extraído el campo y mi mensaje de error
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("El porcentaje no puede ser nulo", resultado.get("porcentajeSimilitud"));
    }
}