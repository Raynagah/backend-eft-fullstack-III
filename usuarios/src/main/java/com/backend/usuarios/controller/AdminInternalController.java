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
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Función: AdminInternalController (Controlador)
 * Título: Controlador Interno de Administración de Usuarios
 * Descripción: Expone endpoints de uso interno (típicamente consumidos por el BFF) para la gestión total de usuarios con privilegios de administrador, permitiendo manipular roles y otros datos sensibles.
 */
@RestController
@RequestMapping("/internal/admin/usuarios") 
@RequiredArgsConstructor
@Tag(name = "Admin Interno", description = "Endpoints internos para gestión total desde el BFF")
public class AdminInternalController {

    private final UsuarioService service;

    /**
     * Función: crearPorAdmin
     * Título: Crear usuario con rol a elección (Solo Admin)
     * Descripción: Permite a un administrador crear un nuevo usuario asignándole un rol específico directamente, eludiendo las restricciones del registro de usuarios estándar.
     *
     * @param dto Objeto UsuarioRequestDTO que contiene los datos del nuevo usuario, incluyendo el rol deseado.
     * @return ResponseEntity con el objeto UsuarioDTO del usuario creado y un código HTTP 201 (CREATED).
     */
    @Operation(summary = "Crear usuario con rol a elección (Solo Admin)")
    @PostMapping
    public ResponseEntity<UsuarioDTO> crearPorAdmin(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(service.crearUsuarioAdmin(dto), HttpStatus.CREATED);
    }

    /**
     * Función: actualizarPorAdmin
     * Título: Actualización total de usuario incluyendo rol (Solo Admin)
     * Descripción: Permite la modificación integral de un perfil de usuario por parte de un administrador, posibilitando el cambio de roles y otros atributos que un usuario normal no puede modificar por sí mismo.
     *
     * @param id Identificador único de tipo Long del usuario que se va a modificar.
     * @param dto Objeto UsuarioUpdateDTO con la información completa a actualizar.
     * @return ResponseEntity con el objeto UsuarioDTO modificado y un código HTTP 200 (OK).
     */
    @Operation(summary = "Actualización total de usuario incluyendo rol (Solo Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPorAdmin(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizarUsuarioPorAdmin(id, dto));
    }
}