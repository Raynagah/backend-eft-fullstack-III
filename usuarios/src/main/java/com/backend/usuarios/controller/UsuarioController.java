package com.backend.usuarios.controller;

import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.usuarios.dto.LoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

/**
 * Función: UsuarioController (Controlador)
 * Título: Controlador de API de Usuarios
 * Descripción: Expone los endpoints REST para la gestión integral de los usuarios, abarcando desde operaciones CRUD hasta la autenticación y manejo de sesiones.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API de gestión de usuarios")
public class UsuarioController {

    private final UsuarioService service;

    /**
     * Función: crear
     * Título: Crear usuario
     * Descripción: Recibe los datos de un nuevo usuario, los valida y solicita al servicio su creación en el sistema.
     *
     * @param dto Objeto UsuarioRequestDTO que contiene la información del usuario a registrar.
     * @return ResponseEntity con el objeto UsuarioDTO del usuario creado y un código HTTP 201 (CREATED).
     */
    @Operation(summary = "Crear usuario")
    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return new ResponseEntity<>(service.crearUsuario(dto), HttpStatus.CREATED);
    }

    /**
     * Función: listar
     * Título: Listar usuarios
     * Descripción: Obtiene y retorna una lista completa de todos los usuarios registrados en la base de datos.
     *
     * @return ResponseEntity que contiene una lista de objetos UsuarioDTO y un código HTTP 200 (OK).
     */
    @Operation(summary = "Listar usuarios")
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /**
     * Función: obtener
     * Título: Obtener usuario por ID
     * Descripción: Busca un usuario en específico a través de su identificador único y retorna sus detalles.
     *
     * @param id Identificador único de tipo Long del usuario buscado.
     * @return ResponseEntity con el objeto UsuarioDTO correspondiente al ID y un código HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    /**
     * Función: login
     * Título: Login de usuario
     * Descripción: Procesa el inicio de sesión validando las credenciales ingresadas. Genera un token JWT, hashea la contraseña para la verificación y maneja las excepciones si las credenciales son incorrectas o hay problemas en el servidor.
     *
     * @param dto Objeto LoginRequestDTO que contiene el correo electrónico y la contraseña ingresada por el usuario.
     * @return ResponseEntity con los datos de sesión (token JWT, usuario, sessionId) si es exitoso, o mensajes de error HTTP 401/500 en caso contrario.
     */
    @Operation(summary = "Login de usuario (retorna JWT, User y SessionId)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        try {
            return ResponseEntity.ok(service.login(dto.correo(), dto.password()));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Captura el 401 del Service y lo envía explícitamente como HTTP 401
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Captura cualquier otro error (ej. Base de datos caída o error de encriptación)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno en ms-usuarios: " + e.getMessage());
        }
    }

    /**
     * Función: validarSesion
     * Título: Validar sesión activa
     * Descripción: Comprueba si existe una sesión válida y activa para un usuario específico utilizando el ID de sesión. Generalmente utilizado por el BFF o API Gateway.
     *
     * @param id Identificador único del usuario de tipo Long.
     * @param sessionId Cadena de texto con el identificador único de la sesión a validar.
     * @return ResponseEntity con un valor Booleano indicando si la sesión es válida (true) o no (false), y un código HTTP 200 (OK).
     */
    @Operation(summary = "Validar sesión activa (Usado por el BFF)")
    @GetMapping("/validar-sesion")
    public ResponseEntity<Boolean> validarSesion(@RequestParam Long id, @RequestParam String sessionId) {
        return ResponseEntity.ok(service.isSesionValida(id, sessionId));
    }

    /**
     * Función: eliminar
     * Título: Eliminar usuario por ID
     * Descripción: Procesa la solicitud para eliminar un usuario del sistema en base a su identificador único.
     *
     * @param id Identificador único de tipo Long del usuario a eliminar.
     * @return ResponseEntity con un mensaje de texto confirmando la eliminación exitosa y un código HTTP 200 (OK).
     */
    @Operation(summary = "Eliminar usuario por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        service.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    /**
     * Función: logout
     * Título: Logout de usuario
     * Descripción: Cierra la sesión activa de un usuario de forma segura basándose en su ID de sesión, invalidándola en el sistema.
     *
     * @param sessionId Cadena de texto con el identificador único de la sesión que se desea cerrar.
     * @return ResponseEntity con un mensaje de confirmación indicando el cierre exitoso y un código HTTP 200 (OK).
     */
    @Operation(summary = "Logout de usuario")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String sessionId) {
        service.logout(sessionId);
        return ResponseEntity.ok("Se ha cerrado sesión");
    }

    /**
     * Función: actualizar
     * Título: Actualizar perfil de usuario
     * Descripción: Permite la modificación de los datos personales de un usuario (como nombres o teléfono), restringiendo de manera explícita el cambio de correo electrónico y contraseña por motivos de seguridad y flujos separados.
     *
     * @param id Identificador único del usuario al que se le aplicarán los cambios.
     * @param dto Objeto UsuarioUpdateDTO con los datos personales a actualizar validados.
     * @return ResponseEntity con el objeto UsuarioDTO ya actualizado y un código HTTP 200 (OK).
     */
    @Operation(summary = "Actualizar perfil de usuario", description = "Permite modificar datos personales. No permite cambiar correo ni contraseña.")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizarUsuario(id, dto));
    }
}