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

/**
 * Función: BffUsuarioController (Controlador BFF)
 * Título: Controlador Web de Usuarios (BFF)
 * Descripción: Gestiona las operaciones de perfil y cuenta de los usuarios desde la perspectiva de la aplicación web. Expone endpoints para consultar y actualizar perfiles personales, obtener el historial de reportes creados por un usuario, gestionar el registro público y proveer listados adaptados para el panel de administración.
 */
@RestController
@RequestMapping("/api/v1/web/usuarios")
@RequiredArgsConstructor
public class BffUsuarioController {

    private final BffUsuarioService bffUsuarioService;
    private final AuthService authService;

    /**
     * Función: obtenerPerfil
     * Título: Obtener perfil de usuario
     * Descripción: Consulta al microservicio subyacente para recuperar los datos detallados de un usuario específico, permitiendo renderizar su vista de perfil personal.
     *
     * @param id Identificador único de tipo Long del usuario a consultar.
     * @return ResponseEntity con el objeto UsuarioDTO que contiene los datos del perfil y un código HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtenerPerfil(@PathVariable Long id) {
        return ResponseEntity.ok(bffUsuarioService.obtenerUsuario(id));
    }

    /**
     * Función: actualizarPerfil
     * Título: Actualizar perfil de usuario
     * Descripción: Recibe los nuevos datos para modificar la información personal de un usuario existente. Delega la ejecución de la actualización al servicio correspondiente.
     *
     * @param id Identificador único de tipo Long del usuario a actualizar.
     * @param actualizacionDTO Objeto UsuarioActualizacionDTO con los campos permitidos para la modificación.
     * @return ResponseEntity con el objeto UsuarioDTO actualizado y un código HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(
            @PathVariable Long id,
            @RequestBody UsuarioActualizacionDTO actualizacionDTO) {
        return ResponseEntity.ok(bffUsuarioService.actualizarUsuario(id, actualizacionDTO));
    }

    /**
     * Función: obtenerMisReportes
     * Título: Obtener reportes vinculados al usuario
     * Descripción: Recupera el historial completo de reportes de mascotas (tanto activos como pasados) generados por un usuario específico. Retorna la información condensada en formato de tarjetas (cards) para facilitar su visualización en el perfil.
     *
     * @param id Identificador único de tipo Long del usuario propietario de los reportes.
     * @return ResponseEntity con una lista de objetos MascotaCardDTO y un código HTTP 200 (OK).
     */
    @GetMapping("/{id}/reportes")
    public ResponseEntity<List<MascotaCardDTO>> obtenerMisReportes(@PathVariable Long id) {
        return ResponseEntity.ok(bffUsuarioService.obtenerReportesPorUsuario(id));
    }

    /**
     * Función: registro
     * Título: Registrar nuevo usuario
     * Descripción: Procesa la solicitud de creación de una cuenta nueva. Maneja de forma controlada las excepciones de comunicación (FeignException), capturando respuestas de error del microservicio (como "Bad Request" o "Correo duplicado") para propagarlas fielmente al cliente web.
     *
     * @param request Objeto UsuarioDTO con los datos iniciales para el registro de la cuenta.
     * @return ResponseEntity con los datos del nuevo usuario (200 OK) si el registro es exitoso, o el cuerpo del error encapsulado con su código HTTP original en caso de fallo.
     */
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

    /**
     * Función: listarParaAdmin
     * Título: Listar usuarios (Formato Admin)
     * Descripción: Recupera un listado completo de los usuarios del sistema, formateado específicamente para las necesidades de visualización del panel de administración web (BFF).
     *
     * @return ResponseEntity con una lista de objetos UsuarioAdminDTO y un código HTTP 200 (OK).
     */
    @GetMapping("/admin/listar")
    public ResponseEntity<List<UsuarioAdminDTO>> listarParaAdmin() {
        return ResponseEntity.ok(bffUsuarioService.listarUsuariosParaAdmin());
    }

}