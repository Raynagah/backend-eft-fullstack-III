package com.backend.usuarios.controller;

import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
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
import com.backend.usuarios.dto.LoginResponseDTO;
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

    //Login con JWT que hashea la contraseña y retorna un token, el usuario y el sessionId
    @Operation(summary = "Login de usuario (retorna JWT, User y SessionId)")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        // Asumimos que ajustaremos el service para que retorne el objeto completo
        return ResponseEntity.ok(service.login(dto.correo(), dto.password()));
    }

    // Endpoint para validar sesión activa, recibe ID de usuario y sessionId, retorna booleano
    @Operation(summary = "Validar sesión activa (Usado por el BFF)")
    @GetMapping("/validar-sesion")
    public ResponseEntity<Boolean> validarSesion(@RequestParam Long id, @RequestParam String sessionId) {
        return ResponseEntity.ok(service.isSesionValida(id, sessionId));
    }

    // Eliminar por ID
    @Operation(summary = "Eliminar usuario por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        service.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // Logout, recibe sessionId y cierra la sesión
    @Operation(summary = "Logout de usuario")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        service.logout(sessionId);
        return ResponseEntity.ok("Se ha cerrado sesión");
    }

    // Actualizar perfil de usuario (sin cambiar correo ni contraseña)
    @Operation(summary = "Actualizar perfil de usuario", description = "Permite modificar datos personales. No permite cambiar correo ni contraseña.")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizarUsuario(id, dto));
    }

}