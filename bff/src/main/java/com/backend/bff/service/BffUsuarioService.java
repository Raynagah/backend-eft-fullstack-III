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

@Service
@RequiredArgsConstructor
public class BffUsuarioService {

    private final UsuarioClient usuarioClient;
    private final MascotasClient mascotaClient;

    public UsuarioDTO obtenerUsuario(Long id) {
        return usuarioClient.obtenerUsuarioPorId(id);
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioActualizacionDTO dto) {
        return usuarioClient.actualizarUsuario(id, dto);
    }

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

    public UsuarioDTO registrar(UsuarioDTO request) {
        return usuarioClient.registrar(request);
    }

    public UsuarioDTO registrarPorAdmin(UsuarioDTO request) {
        return usuarioClient.registrarAdmin(request);
    }

    public UsuarioDTO actualizarUsuarioPorAdmin(Long id, UsuarioActualizacionDTO dto) {
        return usuarioClient.actualizarUsuarioPorAdmin(id, dto);
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioClient.listarUsuarios();
    }

    public void eliminarUsuario(Long id) {
        usuarioClient.eliminarUsuario(id);
    }

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

    // Método privado que ayuda al compilador a inferir el tipo de retorno correctamente
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