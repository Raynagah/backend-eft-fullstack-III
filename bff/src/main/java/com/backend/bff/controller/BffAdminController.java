package com.backend.bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            return ResponseEntity.ok(bffUsuarioService.listarTodos());
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            bffUsuarioService.eliminarUsuario(id);
            // Retornamos un JSON para que Axios en Vue lo procese correctamente
            return ResponseEntity.ok("{\"mensaje\": \"Usuario eliminado correctamente\"}");
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }
}