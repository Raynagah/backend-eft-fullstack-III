package com.backend.bff.service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.GeolocalizacionClient; // Si lo usas más adelante
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO; // El que hicimos antes
import com.backend.bff.dto.WebReporteRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Esto crea el constructor para inyectar los clientes
public class BffMascotaService {

    private final MascotasClient mascotaClient;

    public List<MascotaCardDTO> obtenerDashboard() {
        return mascotaClient.obtenerTodas().stream()
                .map(m -> MascotaCardDTO.builder()
                        .id(m.getId())
                        .nombre(m.getNombre())
                        .resumen(m.getEspecie() + " - " + m.getRaza())
                        .estado(m.getSagaStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public Object crearNuevoReporte(WebReporteRequestDTO dto) {
        // Simulamos que el BFF le asigna el ID del usuario logueado
        // Por ahora, si tu ms-mascotas lo pide, se lo enviamos fijo o lo tomamos del DTO
        System.out.println("BFF: Recibiendo reporte para la mascota: " + dto.getNombre());

        return mascotaClient.crear(dto);
    }
}