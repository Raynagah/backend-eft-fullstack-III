package com.backend.gestionMascotas.repository;

import com.backend.gestionMascotas.model.ReporteMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository 
public interface MascotaRepository extends JpaRepository<ReporteMascota, Long> {

    

    // Buscar todos los reportes por tipo (Ej: "PERDIDA" o "ENCONTRADA")
    List<ReporteMascota> findByTipoReporte(String tipoReporte);

    // Buscar reportes por especie (Ej: "Perro")
    List<ReporteMascota> findByEspecie(String especie);

}
