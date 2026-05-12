package com.backend.bff.controller;

import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.BffUsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Aseguramos la ruta con /api/v1/web para que coincida con tu config de Axios
@RequestMapping("/api/v1/web/usuarios")
@RequiredArgsConstructor
public class BffUsuarioController {

    private final BffUsuarioService bffUsuarioService;

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
}