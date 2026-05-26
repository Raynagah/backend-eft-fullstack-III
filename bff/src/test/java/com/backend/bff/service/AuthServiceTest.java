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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void autenticar_DebeRetornarLoginResponse() {
        LoginRequest request = new LoginRequest();
        LoginResponse response = new LoginResponse();
        when(usuarioClient.login(request)).thenReturn(response);

        LoginResponse result = authService.autenticar(request);

        assertNotNull(result);
        verify(usuarioClient).login(request);
    }

    @Test
    void esSesionValida_DebeRetornarTrue() {
        when(usuarioClient.validarSesion(1L, "token")).thenReturn(true);
        assertTrue(authService.esSesionValida(1L, "token"));
    }

    @Test
    void cerrarSesion_DebeLlamarAlClient() {
        // Si tu cliente devuelve un String, pon un texto cualquiera:
        when(usuarioClient.logout("token")).thenReturn("Sesion cerrada exitosamente");

        // (Nota: Si tu cliente devuelve un ResponseEntity, sería algo como:)
        // when(usuarioClient.logout("token")).thenReturn(org.springframework.http.ResponseEntity.ok().build());

        authService.cerrarSesion("token");

        // Verificamos que el cliente efectivamente fue llamado
        verify(usuarioClient).logout("token");
    }

    @Test
    void registrar_DebeRetornarUsuario() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.registrar(dto)).thenReturn(dto);
        assertNotNull(authService.registrar(dto));
    }
}