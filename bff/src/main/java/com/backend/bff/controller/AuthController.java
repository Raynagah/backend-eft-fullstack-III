package com.backend.bff.controller;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.service.AuthService;
import feign.FeignException;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Si todoo sale bien, devolvemos el token y el 200 OK
            return ResponseEntity.ok(authService.autenticar(request));
        } catch (FeignException e) {
            // Si el ms-usuarios lanza un error (404, 401, etc.),
            // extraemos su mensaje original y se lo pasamos al frontend
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-Session-ID") String sessionId) {
        authService.cerrarSesion(sessionId);
        return ResponseEntity.ok("Sesión cerrada");
    }
}