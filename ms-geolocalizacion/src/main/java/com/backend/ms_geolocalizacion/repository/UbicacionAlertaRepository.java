package com.backend.ms_geolocalizacion.repository;

import com.backend.ms_geolocalizacion.model.UbicacionAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UbicacionAlertaRepository extends JpaRepository<UbicacionAlerta, Long> {
}