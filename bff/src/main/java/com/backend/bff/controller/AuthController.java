package com.backend.bff.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.service.AuthService;

import feign.FeignException;

/**
 * Función: AuthController (Controlador BFF)
 * Título: Controlador de Autenticación (Backend For Frontend)
 * Descripción: Expone los endpoints públicos de seguridad y acceso para las aplicaciones cliente. Actúa como intermediario (BFF) gestionando el inicio de sesión, registro de usuarios y cierre de sesión, delegando la lógica a microservicios subyacentes y manejando las excepciones de comunicación (Feign).
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*") // Permite solicitudes desde cualquier origen 
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Función: login
     * Título: Iniciar sesión
     * Descripción: Recibe las credenciales del usuario y las delega al servicio de autenticación. Gestiona las respuestas del microservicio subyacente, capturando excepciones de Feign para retornar el código HTTP exacto (ej. 401) o un error 500 en caso de fallos de red o problemas inesperados.
     *
     * @param request Objeto LoginRequest que contiene las credenciales ingresadas por el cliente.
     * @return ResponseEntity con el token y datos de acceso si la autenticación es exitosa (200 OK), o el mensaje y código de error correspondiente en caso de fallo.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Si todo sale bien, devolvemos el token y el 200 OK
            return ResponseEntity.ok(authService.autenticar(request));
        } catch (FeignException e) {
            int status = e.status();
            // Si Feign no puede determinar el estatus (-1), lo forzamos a 500
            if (status < 100 || status > 599) {
                status = 500;
            }
            // Retorna el estatus exacto (401) enviado por ms-usuarios
            return ResponseEntity.status(status).body(e.contentUTF8());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error inesperado en el BFF: " + e.getMessage());
        }
    }

    /**
     * Función: registro
     * Título: Registrar nuevo usuario
     * Descripción: Procesa la creación de una cuenta de usuario nueva. Por seguridad, fuerza el rol del usuario a "cliente" para evitar la inyección de roles con privilegios superiores desde el frontend, y limpia la contraseña del objeto de respuesta antes de retornarlo.
     *
     * @param request Objeto UsuarioDTO con los datos del formulario de registro del nuevo usuario.
     * @return ResponseEntity con los datos del usuario creado (omitiendo la contraseña) y un código 200 OK, o el error encapsulado proveniente del microservicio de usuarios.
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody UsuarioDTO request) {
        try {
            // Forzamos el rol "cliente" para que nadie pueda inyectar "admin" desde el formulario público
            request.setTipoUsuario("cliente");

            // El BFF reenvía el usuario al MS
            UsuarioDTO nuevoUsuario = authService.registrar(request);

            // Borramos la contraseña del objeto antes de enviarlo al frontend
            if (nuevoUsuario != null) {
                nuevoUsuario.setPassword(null);
            }

            return ResponseEntity.ok(nuevoUsuario);
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    /**
     * Función: logout
     * Título: Cerrar sesión
     * Descripción: Finaliza la sesión activa del usuario tomando el identificador de sesión a través de las cabeceras HTTP y solicitando su invalidación al servicio de autenticación.
     *
     * @param sessionId Cadena de texto obtenida del header "X-Session-ID" que identifica unívocamente la sesión a cerrar.
     * @return ResponseEntity con un mensaje de confirmación y un código HTTP 200 OK.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-Session-ID") String sessionId) {
        authService.cerrarSesion(sessionId);
        return ResponseEntity.ok("Sesión cerrada");
    }
}