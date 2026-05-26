package com.backend.usuarios.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    // Probamos directamente la clase, no necesitamos Mockito aquí
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void manejarRuntime_DebeRetornarMapaConMensaje() {
        // PREPARACIÓN: Simulamos que en algún lugar del código se lanzó una RuntimeException
        RuntimeException exception = new RuntimeException("Usuario no encontrado");

        // ACCIÓN: Pasamos la excepción manual a nuestro manejador
        Map<String, String> resultado = handler.manejarRuntime(exception);

        // VERIFICACIÓN: Comprobamos que el manejador envolvió el error en el Map correcto
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("mensaje"));
        assertEquals("Usuario no encontrado", resultado.get("mensaje")); // El texto debe coincidir
    }

    @Test
    void manejarGeneral_DebeRetornarErrorInterno() {
        // PREPARACIÓN: Simulamos un fallo catastrófico general
        Exception exception = new Exception("Error inesperado");

        // ACCIÓN
        Map<String, String> resultado = handler.manejarGeneral(exception);

        // VERIFICACIÓN: Evaluamos si oculta el detalle técnico al usuario y muestra un mensaje seguro
        assertNotNull(resultado);
        assertTrue(resultado.containsKey("mensaje"));
        assertEquals("Error interno del servidor", resultado.get("mensaje"));
    }

    @Test
    void manejarValidaciones_DebeMapearLosErroresDeCampos() throws NoSuchMethodException {
        // PREPARACIÓN: Este es el test más complejo. Simulamos que Spring detectó datos inválidos (ej. un DTO malo).
        // 1. Creamos un contenedor de errores falso.
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "usuarioRequestDTO");
        // 2. Le agregamos dos errores de validación simulados.
        bindingResult.addError(new FieldError("usuarioRequestDTO", "correo", "El correo es obligatorio"));
        bindingResult.addError(new FieldError("usuarioRequestDTO", "password", "La contraseña es muy corta"));

        // 3. Generamos parámetros falsos requeridos por la arquitectura interna de Spring para fabricar la excepción
        MethodParameter parameter = new MethodParameter(
                Object.class.getMethod("toString"), -1
        );

        // 4. Instanciamos la excepción exacta que lanza Spring cuando falla un @Valid
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // ACCIÓN: Procesamos nuestra excepción falsa
        Map<String, String> resultado = handler.manejarValidaciones(exception);

        // VERIFICACIÓN: Comprobamos que el Map extrajo limpia y correctamente los campos afectados y sus mensajes
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("El correo es obligatorio", resultado.get("correo"));
        assertEquals("La contraseña es muy corta", resultado.get("password"));
    }
}