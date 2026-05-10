package com.backend.bff.controller;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*") // Ajustar según nuestra configuración de seguridad, ojo acá xd
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.autenticar(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Si las credenciales fallan o el usuario no existe
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-Session-ID") String sessionId) {
        authService.cerrarSesion(sessionId);
        return ResponseEntity.ok("Sesión cerrada");
    }
}