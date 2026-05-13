package com.backend.bff.service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
}