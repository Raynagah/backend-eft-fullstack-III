package com.backend.gestionMascotas.service;

import com.backend.gestionMascotas.model.ReporteMascota;
import com.backend.gestionMascotas.repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Indica que esta clase contiene la lógica de negocio
@RequiredArgsConstructor // Lombok: Crea un constructor automáticamente para inyectar dependencias
public class MascotaService {

    // Inyectamos el Repositorio y la Fábrica
    private final MascotaRepository mascotaRepository;
    private final ReporteFactory reporteFactory;

    // Metodo para crear y guardar un reporte
    public ReporteMascota registrarReporte(String tipo, String especie, String raza, String color, String tamano, String nombre, String telefono, String fotoUrl, Double lat, Double lng) {

        // 1. Usamos el Factory Method para crear la entidad
        ReporteMascota nuevoReporte = reporteFactory.crearReporte(
                tipo, especie, raza, color, tamano, nombre, telefono, fotoUrl, lat, lng
        );

        // 2. Usamos el Repository Pattern para guardarla en la base de datos
        return mascotaRepository.save(nuevoReporte);
    }

    // Metodo para obtener todos los reportes
    public List<ReporteMascota> obtenerTodosLosReportes() {
        return mascotaRepository.findAll();
    }

    // Metodo para buscar por tipo (PERDIDA o ENCONTRADA)
    public List<ReporteMascota> obtenerReportesPorTipo(String tipoReporte) {
        return mascotaRepository.findByTipoReporte(tipoReporte);
    }
}