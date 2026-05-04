package com.backend.bff.service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.GeolocalizacionClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaDetalleCompletoDTO;
import com.backend.bff.dto.WebReporteRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BffMascotaService {

    private final MascotasClient mascotaClient;
    private final GeolocalizacionClient geoClient;

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

    public MascotaDetalleCompletoDTO obtenerDetalleMascota(Long id) {
        // 1. Llamamos a Mascotas
        var mascota = mascotaClient.obtenerPorId(id);

        // 2. Llamamos a Geo (con manejo de error por si el micro está caído)
        Double lat = null;
        Double lon = null;
        try {
            var ubicacion = geoClient.obtenerUbicacionPorId(id);
            if (ubicacion != null) {
                lat = ubicacion.getLatitud();
                lon = ubicacion.getLongitud();
            }
        } catch (Exception e) {
            System.err.println("BFF: ms-geolocalizacion no respondió para el ID " + id);
        }

        // 3. Construimos el DTO para Vue
        return MascotaDetalleCompletoDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .color(mascota.getColor())
                .estadoSaga(mascota.getSagaStatus())
                .latitud(lat)
                .longitud(lon)
                .build();
    }

    public Object crearNuevoReporte(WebReporteRequestDTO dto) {
        System.out.println("BFF: Recibiendo reporte para la mascota: " + dto.getNombre());
        return mascotaClient.crear(dto);
    }
}