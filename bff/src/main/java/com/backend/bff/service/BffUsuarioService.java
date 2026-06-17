package com.backend.bff.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaBaseDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import com.backend.bff.dto.UsuarioAdminDTO;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Función: BffUsuarioService (Servicio)
 * Título: Servicio de Gestión de Usuarios (BFF)
 * Descripción: Actúa como la capa de orquestación para la lógica de negocio relacionada con los usuarios y sus interacciones. Este servicio coordina la comunicación entre el microservicio de usuarios y el de mascotas, permitiendo realizar agregaciones de datos como el conteo de reportes y el formateo de perfiles para la capa web.
 */
@Service
@RequiredArgsConstructor
public class BffUsuarioService {

    private final UsuarioClient usuarioClient;
    private final MascotasClient mascotaClient;

    /**
     * Función: obtenerUsuario
     * Título: Obtener usuario por ID
     * Descripción: Recupera la información completa de un usuario específico delegando la consulta al microservicio de usuarios.
     *
     * @param id Identificador único del usuario.
     * @return UsuarioDTO con los datos del perfil solicitado.
     */
    public UsuarioDTO obtenerUsuario(Long id) {
        return usuarioClient.obtenerUsuarioPorId(id);
    }

    /**
     * Función: actualizarUsuario
     * Título: Actualizar datos de usuario
     * Descripción: Procesa la actualización de la información personal de un usuario enviando los datos al microservicio correspondiente.
     *
     * @param id Identificador único del usuario a actualizar.
     * @param dto Objeto con los datos de actualización permitidos.
     * @return UsuarioDTO con los datos actualizados.
     */
    public UsuarioDTO actualizarUsuario(Long id, UsuarioActualizacionDTO dto) {
        return usuarioClient.actualizarUsuario(id, dto);
    }

    /**
     * Función: obtenerReportesPorUsuario
     * Título: Obtener reportes por usuario
     * Descripción: Filtra el catálogo general de mascotas para recuperar únicamente aquellas creadas por el usuario especificado, transformándolas en objetos de visualización (MascotaCardDTO).
     *
     * @param idUsuario Identificador del usuario propietario de los reportes.
     * @return Lista de MascotaCardDTO asociados al usuario.
     */
    public List<MascotaCardDTO> obtenerReportesPorUsuario(Long idUsuario) {
        return mascotaClient.obtenerTodas().stream()
                .filter(m -> m.getUsuarioId() != null && m.getUsuarioId().equals(idUsuario))
                .map(m -> MascotaCardDTO.builder()
                        .id(m.getId())
                        .nombre(m.getNombre()) 
                        .titulo(m.getTipoReporte() + ": " + m.getEspecie() + " " + m.getRaza())
                        .resumen("Color: " + m.getColor() + " - Tamaño: " + m.getTamano())
                        .estado(m.getSagaStatus())
                        .tipoReporte(m.getTipoReporte())
                        .fotografiaUrl(m.getFotografiaUrl())
                        .fechaReporte(m.getFechaReporte() != null ? m.getFechaReporte().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Función: registrar
     * Título: Registrar nuevo usuario
     * Descripción: Delega el registro de un nuevo usuario al microservicio de persistencia.
     *
     * @param request Datos del nuevo usuario.
     * @return UsuarioDTO registrado.
     */
    public UsuarioDTO registrar(UsuarioDTO request) {
        return usuarioClient.registrar(request);
    }

    /**
     * Función: registrarPorAdmin
     * Título: Registrar usuario (vía Admin)
     * Descripción: Procesa el registro de un nuevo usuario iniciado por un administrador.
     *
     * @param request Datos del usuario.
     * @return UsuarioDTO registrado.
     */
    public UsuarioDTO registrarPorAdmin(UsuarioDTO request) {
        return usuarioClient.registrarAdmin(request);
    }

    /**
     * Función: actualizarUsuarioPorAdmin
     * Título: Actualizar usuario (vía Admin)
     * Descripción: Permite a un administrador modificar los datos de cualquier usuario del sistema.
     *
     * @param id ID del usuario.
     * @param dto Datos de actualización.
     * @return UsuarioDTO actualizado.
     */
    public UsuarioDTO actualizarUsuarioPorAdmin(Long id, UsuarioActualizacionDTO dto) {
        return usuarioClient.actualizarUsuarioPorAdmin(id, dto);
    }

    /**
     * Función: listarTodos
     * Título: Listar todos los usuarios
     * Descripción: Recupera el listado completo de usuarios registrados en el sistema.
     *
     * @return Lista de UsuarioDTO.
     */
    public List<UsuarioDTO> listarTodos() {
        return usuarioClient.listarUsuarios();
    }

    /**
     * Función: eliminarUsuario
     * Título: Eliminar usuario
     * Descripción: Solicita la eliminación de un usuario por su identificador.
     *
     * @param id ID del usuario a eliminar.
     */
    public void eliminarUsuario(Long id) {
        usuarioClient.eliminarUsuario(id);
    }

    /**
     * Función: listarUsuariosParaAdmin
     * Título: Listar usuarios con métricas para Admin
     * Descripción: Genera un listado enriquecido para la vista de administración, combinando la información del usuario con el conteo total de reportes de mascotas que posee en el sistema.
     *
     * @return Lista de UsuarioAdminDTO con estadísticas incluidas.
     */
    public List<UsuarioAdminDTO> listarUsuariosParaAdmin() {
    // 1. Obtener datos de ambos servicios
        List<UsuarioDTO> usuarios = usuarioClient.listarUsuarios();
        
        List<MascotaBaseDTO> todasLasMascotas;
        try {
            todasLasMascotas = mascotaClient.obtenerTodas();
        } catch (Exception e) {
            todasLasMascotas = List.of();
        }

        // 2. Crear el mapa de conteo explícitamente
        Map<Long, Long> conteoReportesPorUsuario = todasLasMascotas.stream()
                .filter(m -> m.getUsuarioId() != null)
                .collect(Collectors.groupingBy(MascotaBaseDTO::getUsuarioId, Collectors.counting()));

        // 3. Mapear utilizando un método de apoyo para evitar errores de inferencia
        return usuarios.stream()
                .map(u -> mapearAUsuarioAdminDTO(u, conteoReportesPorUsuario))
                .collect(Collectors.toList());
    }

    /**
     * Función: mapearAUsuarioAdminDTO
     * Título: Mapear Usuario a DTO Admin
     * Descripción: Método auxiliar privado para transformar un UsuarioDTO en un UsuarioAdminDTO, integrando el contador de reportes calculado previamente.
     *
     * @param u Objeto UsuarioDTO origen.
     * @param conteoMap Mapa con la relación ID Usuario -> Cantidad de Reportes.
     * @return UsuarioAdminDTO formateado.
     */
    private UsuarioAdminDTO mapearAUsuarioAdminDTO(UsuarioDTO u, Map<Long, Long> conteoMap) {
        Integer totalReportes = conteoMap.getOrDefault(u.getId(), 0L).intValue();

        return UsuarioAdminDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getEmail())
                .telefono(u.getTelefono())
                .tipoUsuario(u.getTipoUsuario())
                .ocupacion(u.getOcupacion())
                .cantidadReportes(totalReportes)
                .build();
    }
    
}