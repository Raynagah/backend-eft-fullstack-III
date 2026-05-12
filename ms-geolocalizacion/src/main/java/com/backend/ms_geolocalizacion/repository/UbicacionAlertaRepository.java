package com.backend.ms_geolocalizacion.repository;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UbicacionAlertaRepository extends JpaRepository<UbicacionAlerta, Long> {
    Optional<UbicacionAlerta> findByReporteId(Long reporteId);
}