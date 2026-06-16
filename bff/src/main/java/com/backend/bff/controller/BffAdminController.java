package com.backend.bff.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioAdminDTO;
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
            System.out.println("BFF: Solicitando lista de usuarios al ms-usuarios...");
            List<UsuarioDTO> usuarios = bffUsuarioService.listarTodos();
            
            // Opcional: Limpiamos la contraseña por seguridad
            if(usuarios != null) {
                usuarios.forEach(u -> u.setPassword(null));
            }
            
            System.out.println("BFF: Éxito. Usuarios obtenidos: " + (usuarios != null ? usuarios.size() : 0));
            return ResponseEntity.ok(usuarios);
            
        } catch (FeignException e) {
            System.err.println("BFF: Error de Feign al comunicarse con ms-usuarios: " + e.getMessage());
            int status = e.status();
            
            // Si Feign devuelve -1 (ej. conexión rechazada), forzamos a 500 para que Spring no colapse
            if (status < 100 || status > 599) {
                status = 500;
            }
            return ResponseEntity.status(status).body(e.contentUTF8());
            
        } catch (Exception e) {
            System.err.println("BFF: Error interno totalmente inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"mensaje\": \"Error interno del BFF: " + e.getMessage() + "\"}");
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

    @GetMapping("/admin/listar")
    public ResponseEntity<List<UsuarioAdminDTO>> listarParaAdmin() {
        // LLAMA AL MÉTODO NUEVO, NO AL DE USUARIOCLIENT DIRECTAMENTE
        return ResponseEntity.ok(bffUsuarioService.listarUsuariosParaAdmin());
    }
}