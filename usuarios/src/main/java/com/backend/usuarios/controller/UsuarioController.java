package com.backend.usuarios.controller;

import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.usuarios.dto.LoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API de gestión de usuarios")
public class UsuarioController {

    private final UsuarioService service;

    // Crear usuario
    @Operation(summary = "Crear usuario")
    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(service.crearUsuario(dto), HttpStatus.CREATED);
    }

    // Listar
    @Operation(summary = "Listar usuarios")
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // Obtener por ID
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Login de usuario (retorna JWT)")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(service.login(dto.correo(), dto.password()));
    }

    @Operation(summary = "Eliminar usuario por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        service.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @Operation(summary = "Logout de usuario")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        service.logout(sessionId);
        return ResponseEntity.ok("Se ha cerrado sesión");
    }


}