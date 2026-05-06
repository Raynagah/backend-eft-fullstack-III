package com.backend.bff.service;

import com.backend.bff.client.*;
import com.backend.bff.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BffMascotaService {

    private final MascotasClient mascotaClient;
    private final GeolocalizacionClient geoClient;
    private final UsuarioClient usuarioClient;
    private final CoincidenciasClient coincidenciasClient;
    private final NotificacionClient notificacionClient;

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
        // 1. Datos base de la Mascota
        var mascota = mascotaClient.obtenerPorId(id);

        var detalle = MascotaDetalleCompletoDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .color(mascota.getColor())
                .estadoSaga(mascota.getSagaStatus())
                .build();

        // 2. Integración con Geolocalización
        try {
            var ubicacion = geoClient.obtenerUbicacionPorId(id);
            if (ubicacion != null) {
                detalle.setLatitud(ubicacion.getLatitud());
                detalle.setLongitud(ubicacion.getLongitud());
            }
        } catch (Exception e) {
            System.err.println("BFF Error: ms-geolocalizacion no disponible");
        }

        // 3. Integración con Usuarios
        try {
            var usuario = usuarioClient.obtenerUsuarioPorId(mascota.getUsuarioId());
            detalle.setContactoNombre(usuario.getNombre());
            detalle.setContactoTelefono(usuario.getTelefono());
        } catch (Exception e) {
            detalle.setContactoNombre("Información de contacto no disponible");
        }
        // 4. Integración con Motor de Coincidencias
        try {
            detalle.setPosiblesCoincidencias(coincidenciasClient.obtenerCoincidenciasPorMascota(id));
        } catch (Exception e) {
            detalle.setPosiblesCoincidencias(new ArrayList<>());
            System.err.println("BFF Error: ms-motor-coincidencias no respondió");
        }

        return detalle;
    }

    public Object crearNuevoReporte(WebReporteRequestDTO dto) {
        System.out.println("BFF: Recibiendo reporte para: " + dto.getNombre());

        // 1. Crear la mascota en el microservicio core
        Object response = mascotaClient.crear(dto);

        // 2. Disparar notificación asíncrona (Fire and forget)
        try {
            // Suponiendo que el micro de notificaciones espera un mensaje simple o un DTO
            String mensaje = "Se ha reportado una nueva mascota: " + dto.getNombre();
            notificacionClient.enviarAlertaMascota(new NotificacionRequestDTO(dto.getUsuarioId(), mensaje));
        } catch (Exception e) {
            System.err.println("BFF Error: No se pudo enviar la notificación, pero el reporte fue creado.");
        }

        return response;
    }
}