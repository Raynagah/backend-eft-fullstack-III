package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioAdminDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.BffUsuarioService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.bff.service.AuthService;
import java.util.List;

@RestController
// Aseguramos la ruta con /api/v1/web para que coincida con tu config de Axios
@RequestMapping("/api/v1/web/usuarios")
@RequiredArgsConstructor
public class BffUsuarioController {

    private final BffUsuarioService bffUsuarioService;
    private final AuthService authService;

    // 1. Obtener los datos del perfil
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(bffUsuarioService.obtenerUsuario(id));
    }

    // 2. Actualizar los datos del perfil
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(
            @PathVariable Long id,
            @RequestBody UsuarioActualizacionDTO actualizacionDTO) {
        return ResponseEntity.ok(bffUsuarioService.actualizarUsuario(id, actualizacionDTO));
    }

    // 3. Obtener los reportes (Activos e Historial) de este usuario
    @GetMapping("/{id}/reportes")
    public ResponseEntity<List<MascotaCardDTO>> obtenerMisReportes(@PathVariable Long id) {
        return ResponseEntity.ok(bffUsuarioService.obtenerReportesPorUsuario(id));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody UsuarioDTO request) {
        try {
            // Llamamos al servicio para registrar al usuario
            UsuarioDTO nuevoUsuario = authService.registrar(request);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (FeignException e) {
            // Si el ms-usuarios falla (ej: bad request, correo duplicado),
            // capturamos el error de Feign y lo enviamos al frontend
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    @GetMapping("/admin/listar")
    public ResponseEntity<List<UsuarioAdminDTO>> listarParaAdmin() {
        return ResponseEntity.ok(bffUsuarioService.listarUsuariosParaAdmin());
    }

}