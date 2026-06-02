package com.backend.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;

@FeignClient(name = "ms-usuarios", url = "${services.usuarios.url}")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerUsuarioPorId(@PathVariable("id") Long id);

    @PostMapping("/api/usuarios/login")
    LoginResponse login(@RequestBody LoginRequest dto);

    @GetMapping("/api/usuarios/validar-sesion")
    Boolean validarSesion(@RequestParam("id") Long id, @RequestParam("sessionId") String sessionId);

    @PostMapping("/api/usuarios/logout")
    String logout(@RequestParam("sessionId") String sessionId);

    @PutMapping("/api/usuarios/{id}")
    UsuarioDTO actualizarUsuario(@PathVariable("id") Long id, @RequestBody UsuarioActualizacionDTO dto);

    @PostMapping("/api/usuarios")
    UsuarioDTO registrar(@RequestBody UsuarioDTO dto);

    @PostMapping("/internal/admin/usuarios")
    UsuarioDTO registrarAdmin(@RequestBody UsuarioDTO dto);

    @PutMapping("/internal/admin/usuarios/{id}")
    UsuarioDTO actualizarUsuarioPorAdmin(@PathVariable("id") Long id, @RequestBody UsuarioActualizacionDTO dto);
}