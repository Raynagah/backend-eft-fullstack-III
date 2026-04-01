package com.backend.usuarios.controller;

import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    // Crear usuario
    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(service.crearUsuario(dto), HttpStatus.CREATED);
    }

    // Listar
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }
}