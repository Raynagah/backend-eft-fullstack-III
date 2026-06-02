package com.backend.usuarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/admin/usuarios") // 🔒 Rutas internas exclusivas
@RequiredArgsConstructor
@Tag(name = "Admin Interno", description = "Endpoints internos para gestión total desde el BFF")
public class AdminInternalController {

    private final UsuarioService service;

    @Operation(summary = "Crear usuario con rol a elección (Solo Admin)")
    @PostMapping
    public ResponseEntity<Usuario> crearPorAdmin(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(service.crearUsuarioAdmin(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualización total de usuario incluyendo rol (Solo Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarPorAdmin(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizarUsuarioPorAdmin(id, dto));
    }
}