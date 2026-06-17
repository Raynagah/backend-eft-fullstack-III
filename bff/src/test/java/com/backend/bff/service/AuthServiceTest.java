package com.backend.bff.service;

import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.dto.UsuarioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Función: AuthServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Autenticación (BFF)
 * Descripción: Verifica la correcta delegación de las operaciones de seguridad 
 * hacia el microservicio de usuarios a través del cliente Feign. Incluye pruebas 
 * para el inicio de sesión, validación de sesiones activas, cierre de sesión y registro.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private AuthService authService;

    /**
     * Función: autenticar_DebeRetornarLoginResponse
     * Título: Validar inicio de sesión
     * Descripción: Comprueba que el servicio reciba correctamente la petición de login 
     * y delegue la operación al cliente Feign, retornando la respuesta estructurada 
     * con los tokens de acceso correspondientes.
     */
    @Test
    void autenticar_DebeRetornarLoginResponse() {
        LoginRequest request = new LoginRequest();
        LoginResponse response = new LoginResponse();
        
        when(usuarioClient.login(request)).thenReturn(response);

        LoginResponse result = authService.autenticar(request);

        assertNotNull(result);
        verify(usuarioClient, times(1)).login(request);
    }

    /**
     * Función: esSesionValida_DebeRetornarTrue
     * Título: Validar sesión activa (Verdadero)
     * Descripción: Verifica que el servicio de autenticación retorne verdadero (true) 
     * cuando el cliente Feign confirma que la sesión consultada es válida y se encuentra activa.
     */
    @Test
    void esSesionValida_DebeRetornarTrue() {
        when(usuarioClient.validarSesion(1L, "token_valido")).thenReturn(true);
        
        boolean result = authService.esSesionValida(1L, "token_valido");
        
        assertTrue(result);
        verify(usuarioClient, times(1)).validarSesion(1L, "token_valido");
    }

    /**
     * Función: esSesionValida_DebeRetornarFalse
     * Título: Validar sesión inactiva (Falso)
     * Descripción: Comprueba que el servicio de autenticación retorne falso (false) 
     * cuando el cliente Feign indica que la sesión consultada ya no es válida o ha expirado.
     */
    @Test
    void esSesionValida_DebeRetornarFalse() {
        when(usuarioClient.validarSesion(1L, "token_invalido")).thenReturn(false);
        
        boolean result = authService.esSesionValida(1L, "token_invalido");
        
        assertFalse(result);
        verify(usuarioClient, times(1)).validarSesion(1L, "token_invalido");
    }

    /**
     * Función: cerrarSesion_DebeLlamarAlClient
     * Título: Validar cierre de sesión
     * Descripción: Asegura que la petición para invalidar y cerrar una sesión específica 
     * se comunique y delegue de manera correcta al microservicio subyacente.
     */
    @Test
    void cerrarSesion_DebeLlamarAlClient() {
        // Ejecutamos el cierre de sesión enviando un token simulado
        authService.cerrarSesion("token_para_cerrar");

        // Verificamos que el cliente de Feign haya sido llamado exactamente una vez
        verify(usuarioClient, times(1)).logout("token_para_cerrar");
    }

    /**
     * Función: registrar_DebeRetornarUsuario
     * Título: Validar registro de nuevo usuario
     * Descripción: Garantiza que los datos de creación para una nueva cuenta se envíen 
     * correctamente al microservicio de usuarios y el servicio retorne el DTO con 
     * la información procesada.
     */
    @Test
    void registrar_DebeRetornarUsuario() {
        UsuarioDTO dto = new UsuarioDTO();
        
        when(usuarioClient.registrar(dto)).thenReturn(dto);
        
        UsuarioDTO result = authService.registrar(dto);
        
        assertNotNull(result);
        verify(usuarioClient, times(1)).registrar(dto);
    }
}