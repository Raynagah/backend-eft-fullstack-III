package com.backend.bff.service;

import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioClient usuarioClient;

    public LoginResponse autenticar(LoginRequest request) {
        // El Feign Client hace la llamada HTTP al microservicio de Usuarios.
        // Si el login falla allá (por ejemplo, tira un 401), Feign propagará la excepción.
        return usuarioClient.login(request);
    }

    public boolean esSesionValida(Long usuarioId, String sessionIdRecibido) {
        // Le preguntamos al dueño de la base de datos (Neon) si la sesión sigue viva
        return usuarioClient.validarSesion(usuarioId, sessionIdRecibido);
    }
}