package com.backend.bff.service;

import com.backend.bff.client.*;
import com.backend.bff.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Función: BffMascotaService (Servicio)
 * Título: Servicio Orquestador de Mascotas (BFF)
 * Descripción: Actúa como el orquestador principal para la lógica de negocio de las mascotas en el Backend For Frontend. Es responsable de agregar información proveniente de múltiples microservicios (mascotas, geolocalización, usuarios y motor de coincidencias) para consolidar respuestas completas destinadas a la interfaz web.
 */
@Service
@RequiredArgsConstructor
public class BffMascotaService {

    private final MascotasClient mascotaClient;
    private final GeolocalizacionClient geoClient;
    private final UsuarioClient usuarioClient;
    private final CoincidenciasClient coincidenciasClient;

    /**
     * Función: obtenerDashboard
     * Título: Obtener datos para dashboard
     * Descripción: Recupera el listado completo de mascotas desde el microservicio principal y lo transforma en una lista de objetos MascotaCardDTO, mapeando y formateando los campos necesarios para la visualización eficiente en el frontend.
     *
     * @return Lista de objetos MascotaCardDTO listos para renderizar en el dashboard.
     */
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
                        .fechaReporte(m.getFechaReporte())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Función: obtenerDetalleMascota
     * Título: Obtener detalle completo y enriquecido
     * Descripción: Construye un objeto MascotaDetalleCompletoDTO mediante un proceso de agregación. Inicia con los datos base de la mascota y enriquece la información consultando los servicios de geolocalización, usuarios y el motor de coincidencias, manejando errores de forma tolerante para garantizar que la respuesta principal siempre se entregue.
     *
     * @param id Identificador único de la mascota.
     * @return Objeto MascotaDetalleCompletoDTO con toda la información consolidada.
     */
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

        // 2. Integración con Geolocalización
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

        // 3. Integración con Usuarios
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

    /**
     * Función: crearNuevoReporte
     * Título: Crear nuevo reporte y procesar coincidencias
     * Descripción: Coordina el flujo de creación de un nuevo reporte: primero registra la mascota en el microservicio core y, tras extraer el ID generado, solicita al motor de coincidencias que evalúe posibles cruces (notificaciones) relacionados con este nuevo registro.
     *
     * @param dto Datos del reporte enviado desde el formulario web.
     * @return Respuesta del microservicio core tras la creación.
     */
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
                coincidenciasClient.procesarCoincidencias(nuevaMascotaId);
            } catch (Exception e) {
                System.err.println("ERROR REAL EN COINCIDENCIAS (AL CREAR): " + e.getMessage());
            }
        }

        return response;
    }

    /**
     * Función: eliminarReporte
     * Título: Eliminar reporte
     * Descripción: Delega la solicitud de eliminación de un reporte al microservicio de mascotas.
     *
     * @param id Identificador único del reporte a eliminar.
     */
    public void eliminarReporte(Long id) {
    System.out.println("BFF: Solicitando eliminación del reporte ID: " + id);
    mascotaClient.eliminar(id);
    }
}