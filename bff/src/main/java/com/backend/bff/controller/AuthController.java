package com.backend.bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.AuthService;

import feign.FeignException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*") // Ajustar según nuestra configuración de seguridad, ojo acá xd
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Si todo sale bien, devolvemos el token y el 200 OK
            return ResponseEntity.ok(authService.autenticar(request));
        } catch (FeignException e) {
            int status = e.status();
            // Si Feign no puede determinar el estatus (-1), lo forzamos a 500
            if (status < 100 || status > 599) {
                status = 500;
            }
            // Retorna el estatus exacto (401) enviado por ms-usuarios
            return ResponseEntity.status(status).body(e.contentUTF8());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error inesperado en el BFF: " + e.getMessage());
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody UsuarioDTO request) {
        try {
            // ¡MEDIDA DE SEGURIDAD CRÍTICA!
            // Forzamos el rol "cliente" para que nadie pueda inyectar "admin" desde el formulario público
            request.setTipoUsuario("cliente");

            // El BFF reenvía el usuario al MS
            UsuarioDTO nuevoUsuario = authService.registrar(request);

            // Borramos la contraseña del objeto antes de enviarlo al frontend
            if (nuevoUsuario != null) {
                nuevoUsuario.setPassword(null);
            }

            return ResponseEntity.ok(nuevoUsuario);
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-Session-ID") String sessionId) {
        authService.cerrarSesion(sessionId);
        return ResponseEntity.ok("Sesión cerrada");
    }
}