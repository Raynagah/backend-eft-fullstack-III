package com.backend.ms_geolocalizacion.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.ms_geolocalizacion.exception.BadRequestException;
import com.backend.ms_geolocalizacion.exception.ResourceNotFoundException;
import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import com.backend.ms_geolocalizacion.repository.UbicacionAlertaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Función: GeoService (Servicio)
 * Título: Servicio de Geolocalización
 * Descripción: Gestiona la lógica de negocio para las ubicaciones espaciales, incluyendo el registro de coordenadas, su validación matemática, y el cálculo avanzado de distancias (mediante la fórmula de Haversine) para localizar alertas dentro de un radio de proximidad.
 */
@Service
@RequiredArgsConstructor
public class GeoService {

    private final UbicacionAlertaRepository repository;
    private static final double RADIO_TIERRA_KM = 6371.0;

    /**
     * Función: registrarUbicacion
     * Título: Registrar ubicación de alerta
     * Descripción: Verifica que la alerta contenga un ID de reporte asociado y valida matemáticamente que las coordenadas sean correctas antes de persistir la información en la base de datos.
     *
     * @param ubicacion Objeto UbicacionAlerta con los datos geográficos y el reporte a vincular.
     * @return El objeto UbicacionAlerta persistido en la base de datos.
     * @throws BadRequestException Si falta el ID del reporte o las coordenadas son inválidas.
     */
    public UbicacionAlerta registrarUbicacion(UbicacionAlerta ubicacion) {
        // Validación de datos requeridos
        if (ubicacion.getReporteId() == null) {
            throw new BadRequestException("El ID de reporte es obligatorio.");
        }
        validarCoordenadas(ubicacion.getLatitud(), ubicacion.getLongitud());
        
        return repository.save(ubicacion);
    }

    /**
     * Función: obtenerTodas
     * Título: Obtener todas las ubicaciones
     * Descripción: Consulta el repositorio para recuperar el listado completo de ubicaciones espaciales de todas las alertas registradas.
     *
     * @return Lista completa de objetos UbicacionAlerta.
     */
    public List<UbicacionAlerta> obtenerTodas() {
        return repository.findAll();
    }

    /**
     * Función: buscarCercanas
     * Título: Buscar ubicaciones cercanas
     * Descripción: Compara un punto geográfico de origen contra todas las ubicaciones registradas y filtra aquellas que se encuentren dentro del radio máximo en kilómetros especificado.
     *
     * @param miLat Latitud del punto de origen.
     * @param miLon Longitud del punto de origen.
     * @param radioMaxKm Radio máximo de búsqueda expresado en kilómetros.
     * @return Lista de objetos UbicacionAlerta que se encuentran dentro del radio indicado.
     * @throws BadRequestException Si las coordenadas de origen son inválidas o si el radio es nulo o menor/igual a cero.
     */
    public List<UbicacionAlerta> buscarCercanas(Double miLat, Double miLon, Double radioMaxKm) {
        validarCoordenadas(miLat, miLon);
        if (radioMaxKm == null || radioMaxKm <= 0) {
            throw new BadRequestException("El radio de búsqueda debe ser mayor a 0 KM.");
        }

        List<UbicacionAlerta> todas = repository.findAll();
        return todas.stream()
                .filter(u -> calcularDistanciaKm(miLat, miLon, u.getLatitud(), u.getLongitud()) <= radioMaxKm)
                .collect(Collectors.toList());
    }

    /**
     * Función: eliminarUbicacion
     * Título: Eliminar ubicación
     * Descripción: Busca un registro de ubicación mediante su ID y lo elimina del sistema. Lanza una excepción si el identificador no existe en la base de datos.
     *
     * @param id Identificador único de tipo Long de la ubicación a eliminar.
     * @throws ResourceNotFoundException Si no se encuentra un registro con el ID proporcionado.
     */
    public void eliminarUbicacion(Long id) {
        UbicacionAlerta ubicacion = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ninguna ubicación con el ID: " + id));
        repository.delete(ubicacion);
    }

    /**
     * Función: obtenerPorReporteId
     * Título: Obtener ubicación por ID de reporte
     * Descripción: Realiza una búsqueda exacta en la base de datos para devolver la ubicación espacial que corresponde de forma única a un ID de reporte.
     *
     * @param reporteId Identificador único de tipo Long del reporte.
     * @return El objeto UbicacionAlerta asociado al reporte.
     * @throws ResourceNotFoundException Si el reporte no tiene ninguna ubicación asociada.
     */
    public UbicacionAlerta obtenerPorReporteId(Long reporteId) {
        return repository.findByReporteId(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ninguna ubicación asociada al reporte con ID: " + reporteId));
    }

    /**
     * Función: validarCoordenadas
     * Título: Validar coordenadas geográficas
     * Descripción: Método utilitario privado que comprueba estructuralmente que los valores de latitud estén entre -90.0 y 90.0, y los de longitud entre -180.0 y 180.0.
     *
     * @param lat Valor de la latitud a evaluar.
     * @param lon Valor de la longitud a evaluar.
     * @throws BadRequestException Si los valores son nulos o se encuentran fuera de los rangos geográficos estándar.
     */
    // Método utilitario privado para centralizar la validación matemática de coordenadas
    private void validarCoordenadas(Double lat, Double lon) {
        if (lat == null || lat < -90.0 || lat > 90.0) {
            throw new BadRequestException("La latitud debe ser un valor numérico válido entre -90.0 y 90.0.");
        }
        if (lon == null || lon < -180.0 || lon > 180.0) {
            throw new BadRequestException("La longitud debe ser un valor numérico válido entre -180.0 y 180.0.");
        }
    }

    /**
     * Función: calcularDistanciaKm
     * Título: Calcular distancia en kilómetros (Haversine)
     * Descripción: Implementación matemática de la fórmula del semiverseno (Haversine) para calcular la distancia del círculo máximo entre dos puntos de una esfera a partir de sus longitudes y latitudes.
     *
     * @param lat1 Latitud del punto de origen.
     * @param lon1 Longitud del punto de origen.
     * @param lat2 Latitud del punto de destino.
     * @param lon2 Longitud del punto de destino.
     * @return La distancia expresada en kilómetros (tipo double) entre ambos puntos.
     */
    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RADIO_TIERRA_KM * c;
    }
}