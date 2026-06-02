package com.backend.bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.BffUsuarioService;

import feign.FeignException;

@RestController
@RequestMapping("/api/v1/web/admin/usuarios")
@CrossOrigin(origins = "*") 
public class BffAdminController {

    @Autowired
    private BffUsuarioService bffUsuarioService;

    @PostMapping
    public ResponseEntity<?> crearUsuarioPorAdmin(@RequestBody UsuarioDTO request) {
        try {
            // Aquí NO forzamos el rol a cliente, porque el admin sí puede elegir crear otros admins o clientes
            UsuarioDTO nuevoUsuario = bffUsuarioService.registrarPorAdmin(request);
            if (nuevoUsuario != null) nuevoUsuario.setPassword(null);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuarioPorAdmin(@PathVariable Long id, @RequestBody UsuarioActualizacionDTO request) {
        try {
            UsuarioDTO usuarioActualizado = bffUsuarioService.actualizarUsuarioPorAdmin(id, request);
            if (usuarioActualizado != null) usuarioActualizado.setPassword(null);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }
}