package com.backend.bff.service;

import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.LoginRequest;
import com.backend.bff.dto.LoginResponse;
import com.backend.bff.dto.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Función: AuthService (Servicio)
 * Título: Servicio de Autenticación (BFF)
 * Descripción: Actúa como la capa de abstracción (BFF) para las operaciones de seguridad. Centraliza la comunicación con el microservicio de usuarios mediante Feign, gestionando los flujos de inicio de sesión, validación de sesiones, cierre de sesión y registro de nuevos usuarios desde el frontend.
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioClient usuarioClient;

    /**
     * Función: autenticar
     * Título: Autenticar usuario
     * Descripción: Envía las credenciales proporcionadas al microservicio de usuarios para validar el acceso. Si la autenticación falla, la excepción de Feign es propagada al controlador para su manejo.
     *
     * @param request Objeto LoginRequest que contiene las credenciales de acceso (ej. usuario/correo y contraseña).
     * @return Objeto LoginResponse con los tokens de sesión o datos de acceso generados.
     */
    public LoginResponse autenticar(LoginRequest request) {
        // El Feign Client hace la llamada HTTP al microservicio de Usuarios.
        // Si el login falla allá, Feign propagará la excepción.
        return usuarioClient.login(request);
    }

    /**
     * Función: esSesionValida
     * Título: Validar sesión
     * Descripción: Consulta al microservicio backend si la combinación de ID de usuario y el identificador de sesión proporcionado es válida y se encuentra activa.
     *
     * @param usuarioId Identificador único del usuario.
     * @param sessionIdRecibido Identificador de la sesión a verificar.
     * @return booleano que indica true si la sesión es válida o false en caso contrario.
     */
    public boolean esSesionValida(Long usuarioId, String sessionIdRecibido) {
        // Le preguntamos a la bd si la sesión sigue viva
        return usuarioClient.validarSesion(usuarioId, sessionIdRecibido);
    }

    /**
     * Función: cerrarSesion
     * Título: Cerrar sesión
     * Descripción: Solicita al microservicio de backend la invalidación y cierre de la sesión especificada, eliminándola del sistema.
     *
     * @param sessionId Identificador único de la sesión que se desea cerrar.
     */
    public void cerrarSesion(String sessionId) {
        usuarioClient.logout(sessionId);
    }

    /**
     * Función: registrar
     * Título: Registrar nuevo usuario
     * Descripción: Delega la solicitud de creación de una nueva cuenta de usuario al microservicio correspondiente, actuando como intermediario.
     *
     * @param request Objeto UsuarioDTO que contiene los datos necesarios para registrar al usuario.
     * @return Objeto UsuarioDTO con la información del usuario recién registrado en el sistema.
     */
    public UsuarioDTO registrar(UsuarioDTO request) {
        // Simplemente delegamos la creación al microservicio de usuarios
        return usuarioClient.registrar(request);
    }
}