package com.backend.bff.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;

import lombok.RequiredArgsConstructor;

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
        // PLAN B: Traemos todas y el BFF filtra las que coincidan con el idUsuario
        return mascotaClient.obtenerTodas().stream()
                .filter(m -> m.getUsuarioId() != null && m.getUsuarioId().equals(idUsuario)) // El filtro mágico
                .map(m -> MascotaCardDTO.builder()
                        .id(m.getId())
                        .nombre(m.getTipoReporte() + ": " + m.getEspecie() + " " + m.getRaza())
                        .resumen("Color: " + m.getColor() + " - Tamaño: " + m.getTamano())
                        .estado(m.getSagaStatus())
                        .tipoReporte(m.getTipoReporte())
                        .fotografiaUrl(m.getFotografiaUrl())
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
}