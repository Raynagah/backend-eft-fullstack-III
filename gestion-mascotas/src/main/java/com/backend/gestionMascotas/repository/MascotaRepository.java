package com.backend.gestionMascotas.repository;

import com.backend.gestionMascotas.model.ReporteMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Le indica a Spring que este es un componente de acceso a datos
public interface MascotaRepository extends JpaRepository<ReporteMascota, Long> {

    // Spring Data JPA hace la "magia" de crear las consultas SQL por ti
    // Solo con declarar estos métodos, ya puedes buscar en la base de datos:

    // Buscar todos los reportes por tipo (Ej: "PERDIDA" o "ENCONTRADA")
    List<ReporteMascota> findByTipoReporte(String tipoReporte);

    // Buscar reportes por especie (Ej: "Perro")
    List<ReporteMascota> findByEspecie(String especie);

    // Buscar reportes por ciudad/ubicación aproximada (Si agregaras ese campo)
    // List<ReporteMascota> findByCiudad(String ciudad);
}