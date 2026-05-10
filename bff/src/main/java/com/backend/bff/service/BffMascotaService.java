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
                        // Como no hay 'nombre', armamos un título descriptivo para el Frontend
                        .nombre(m.getTipoReporte() + ": " + m.getEspecie() + " " + m.getRaza())
                        .resumen("Color: " + m.getColor() + " - Tamaño: " + m.getTamano())
                        .estado(m.getSagaStatus()) // Ahora coincide
                        .build())
                .collect(Collectors.toList());
    }

    public MascotaDetalleCompletoDTO obtenerDetalleMascota(Long id) {
        // 1. Datos base de la Mascota
        var mascota = mascotaClient.obtenerPorId(id);

        var detalle = MascotaDetalleCompletoDTO.builder()
                .id(mascota.getId())
                .tipoReporte(mascota.getTipoReporte())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .color(mascota.getColor())
                .tamano(mascota.getTamano())
                .fotografiaUrl(mascota.getFotografiaUrl())
                .sagaStatus(mascota.getSagaStatus())
                .fechaReporte(mascota.getFechaReporte())
                // Asignamos POR DEFECTO los datos que ya vienen del ms-mascotas
                .latitud(mascota.getLatitud())
                .longitud(mascota.getLongitud())
                .contactoNombre(mascota.getNombreContacto())
                .contactoTelefono(mascota.getTelefonoContacto())
                .contactoEmail(mascota.getEmailContacto())
                .build();

        // 2. Integración con Geolocalización (Enriquecimiento)
        try {
            var ubicacion = geoClient.obtenerUbicacionPorId(id);
            if (ubicacion != null && ubicacion.getLatitud() != null) {
                // Si Geo responde, sobreescribimos con datos más precisos
                detalle.setLatitud(ubicacion.getLatitud());
                detalle.setLongitud(ubicacion.getLongitud());
            }
        } catch (Exception e) {
            //System.err.println("BFF Info: ms-geo falló. Usando coordenadas base de la mascota.");
            System.err.println("ERROR REAL EN COINCIDENCIAS: " + e.getMessage());
        }

        // 3. Integración con Usuarios (Enriquecimiento)
        try {
            var usuario = usuarioClient.obtenerUsuarioPorId(mascota.getUsuarioId());
            if (usuario != null && usuario.getNombre() != null) {
                // Si el micro de Usuarios responde, actualizamos con la info fresca del perfil
                detalle.setContactoNombre(usuario.getNombre());
                detalle.setContactoTelefono(usuario.getTelefono());
                detalle.setContactoEmail(usuario.getEmail());

            }
        } catch (Exception e) {
            System.err.println("Error real en ms-usuarios: " + e.getMessage());
            e.printStackTrace(); // Esto dirá en la consola si es un 404 (ruta) o error de mapeo
            //System.err.println("BFF Info: ms-usuarios falló. Usando contacto base de la mascota.");
        }

        // 4. Integración con Motor de Coincidencias
        try {
            detalle.setPosiblesCoincidencias(coincidenciasClient.obtenerCoincidenciasPorMascota(id));
        } catch (Exception e) {
            detalle.setPosiblesCoincidencias(new ArrayList<>());
            //System.err.println("BFF Error: ms-motor-coincidencias no respondió");
            // Esto te dirá si es un 404 (Ruta mal), 500 (Error en el micro) o Connection Refused (Puerto mal)
            System.err.println("ERROR REAL EN COINDICENCIAS: " + e.getMessage());

        }

        return detalle;
    }

    public Object crearNuevoReporte(WebReporteRequestDTO dto) {
        // Ajustamos el log ya que dto.getNombre() probablemente tampoco exista
        System.out.println("BFF: Recibiendo reporte para: " + dto.getEspecie() + " " + dto.getRaza());

        // 1. Crear la mascota en el microservicio core
        Object response = mascotaClient.crear(dto);

        // 2. Disparar notificación asíncrona (Fire and forget)
        try {
            String mensaje = "Se ha reportado: " + dto.getEspecie() + " " + dto.getRaza();
            notificacionClient.enviarAlertaMascota(new NotificacionRequestDTO(dto.getUsuarioId(), mensaje));
        } catch (Exception e) {
            //System.err.println("BFF Error: No se pudo enviar la notificación, pero el reporte fue creado.");
            // Esto te dirá si es un 404 (Ruta mal), 500 (Error en el micro) o Connection Refused (Puerto mal)
            System.err.println("ERROR REAL EN NOTIFICACIONES: " + e.getMessage());
        }

        return response;
    }
}