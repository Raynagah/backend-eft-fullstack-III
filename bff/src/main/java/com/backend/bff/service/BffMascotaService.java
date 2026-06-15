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
                        .titulo(m.getTipoReporte() + ": " + m.getEspecie() + " " + m.getRaza())
                        .resumen("Color: " + m.getColor() + " - Tamaño: " + m.getTamano())
                        .estado(m.getSagaStatus())
                        .tipoReporte(m.getTipoReporte())
                        .fotografiaUrl(m.getFotografiaUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public MascotaDetalleCompletoDTO obtenerDetalleMascota(Long id) {
        // 1. Datos base de la Mascota
        var mascota = mascotaClient.obtenerPorId(id);

        var detalle = MascotaDetalleCompletoDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
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
            System.err.println("ERROR REAL EN GEOLOCALIZACION: " + e.getMessage());
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

        // 4. Integración con Motor de Coincidencias (Enriquecimiento de Coincidencias)
        try {
            // A. Obtenemos la lista básica de coincidencias desde el microservicio
            List<CoincidenciaDTO> coincidencias = coincidenciasClient.obtenerCoincidenciasPorMascota(id);
            
            // B. Recorremos cada coincidencia para averiguar su tipo de reporte real
            if (coincidencias != null && !coincidencias.isEmpty()) {
                for (CoincidenciaDTO coincidencia : coincidencias) {
                    try {
                        // Usamos el cliente de mascotas para traer los datos del reporte clon/match
                        var datosMascotaMatch = mascotaClient.obtenerPorId(coincidencia.getMascotaId());
                        if (datosMascotaMatch != null) {
                            // Seteamos el tipo de reporte ("PERDIDA" o "ENCONTRADA") en el DTO
                            coincidencia.setTipoReporte(datosMascotaMatch.getTipoReporte());
                        }
                    } catch (Exception ex) {
                        System.err.println("BFF Alerta: No se pudo enriquecer el tipoReporte para la coincidencia ID " 
                                + coincidencia.getMascotaId() + ". Detalle: " + ex.getMessage());
                    }
                }
            }
            
            // C. Asignamos la lista ya enriquecida al detalle completo
            detalle.setPosiblesCoincidencias(coincidencias);
            
        } catch (Exception e) {
            detalle.setPosiblesCoincidencias(new ArrayList<>());
            System.err.println("ERROR REAL EN COINCIDENCIAS: " + e.getMessage());
        }

        return detalle;
    }

    public Object crearNuevoReporte(WebReporteRequestDTO dto) {
        String nombreMascota = (dto.getNombre() != null && !dto.getNombre().isBlank()) ? dto.getNombre() : "Sin nombre";
        System.out.println("BFF: Recibiendo reporte para: " + nombreMascota + " (" + dto.getEspecie() + " " + dto.getRaza() + ")");

        // 1. Crear la mascota en el microservicio core
        Object response = mascotaClient.crear(dto);

        // 2. Extraer el ID de la mascota recién creada (Feign devuelve Object como Map usualmente)
        Long nuevaMascotaId = null;
        try {
            if (response instanceof java.util.Map) {
                nuevaMascotaId = Long.valueOf(((java.util.Map<?, ?>) response).get("id").toString());
            }
        } catch (Exception e) {
            System.err.println("No se pudo extraer el ID de la respuesta.");
        }

        // 3. Llamar al motor para que evalúe y notifique de verdad (solo si corresponde)
        if (nuevaMascotaId != null) {
            try {
                // NOTA: Asegúrate de agregar el método @PostMapping("/api/coincidencias/procesar/{idReporte}") 
                // a tu interface CoincidenciasClient antes de ejecutar esto.
                coincidenciasClient.procesarCoincidencias(nuevaMascotaId);
            } catch (Exception e) {
                System.err.println("ERROR REAL EN COINCIDENCIAS (AL CREAR): " + e.getMessage());
            }
        }

        return response;
    }

    public void eliminarReporte(Long id) {
    System.out.println("BFF: Solicitando eliminación del reporte ID: " + id);
    mascotaClient.eliminar(id);
    }
}