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

/**
 * Función: BffAdminController (Controlador BFF)
 * Título: Controlador de Administración de Usuarios (BFF)
 * Descripción: Expone los endpoints de administración para el panel de control web. Permite a los administradores gestionar cuentas de usuario (crear, actualizar, listar y eliminar), delegando la ejecución al microservicio subyacente y gestionando la presentación de errores mediante Feign.
 */
@RestController
@RequestMapping("/api/v1/web/admin/usuarios")
@CrossOrigin(origins = "*") 
public class BffAdminController {

    @Autowired
    private BffUsuarioService bffUsuarioService;

    /**
     * Función: crearUsuarioPorAdmin
     * Título: Crear usuario por administrador
     * Descripción: Procesa la creación de un nuevo usuario desde el panel de administración. A diferencia del registro público, este método permite asignar explícitamente el rol del usuario (por ejemplo, crear otros administradores). Retorna el usuario sin exponer la contraseña.
     *
     * @param request Objeto UsuarioDTO con la información requerida para el nuevo usuario.
     * @return ResponseEntity con los datos del usuario creado y código 200 OK, o el error propagado por Feign en caso de fallo.
     */
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

    /**
     * Función: actualizarUsuarioPorAdmin
     * Título: Actualizar usuario
     * Descripción: Sobrescribe los datos de un usuario existente basándose en su ID. Limpia la contraseña del objeto de retorno para evitar filtraciones de seguridad.
     *
     * @param id Identificador único de tipo Long del usuario a actualizar.
     * @param request Objeto UsuarioActualizacionDTO que contiene los nuevos datos a persistir.
     * @return ResponseEntity con el objeto UsuarioDTO actualizado y un código 200 OK, o el error correspondiente capturado por Feign.
     */
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

    /**
     * Función: listarUsuarios
     * Título: Listar todos los usuarios
     * Descripción: Solicita al microservicio de usuarios el listado íntegro de cuentas registradas. Realiza un proceso de limpieza eliminando las contraseñas de cada registro antes de despacharlos al frontend. Maneja robustamente excepciones de comunicación e internas.
     *
     * @return ResponseEntity con una lista de objetos UsuarioDTO limpios (200 OK), o respuestas de error estructuradas con los códigos HTTP pertinentes (ej. 500 para fallos graves).
     */
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            System.out.println("BFF: Solicitando lista de usuarios al ms-usuarios...");
            List<UsuarioDTO> usuarios = bffUsuarioService.listarTodos();
            
            // Limpiamos la contraseña por seguridad
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

    /**
     * Función: eliminarUsuario
     * Título: Eliminar usuario por ID
     * Descripción: Delega la instrucción de borrado lógico o físico de una cuenta de usuario al microservicio correspondiente. Retorna un objeto JSON estandarizado para facilitar su consumo por clientes HTTP como Axios.
     *
     * @param id Identificador único de tipo Long de la cuenta a eliminar.
     * @return ResponseEntity conteniendo un JSON con un mensaje de éxito (200 OK), o el cuerpo del error si la operación falla a través de Feign.
     */
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

    /**
     * Función: listarParaAdmin
     * Título: Listar usuarios (Formato Admin)
     * Descripción: Recupera un listado de usuarios pre-formateado y mapeado a DTOs específicos de administración, optimizando los datos para la interfaz visual del panel de control.
     *
     * @return ResponseEntity con una lista especializada de objetos UsuarioAdminDTO y un código 200 OK.
     */
    @GetMapping("/admin/listar")
    public ResponseEntity<List<UsuarioAdminDTO>> listarParaAdmin() {
        return ResponseEntity.ok(bffUsuarioService.listarUsuariosParaAdmin());
    }
}