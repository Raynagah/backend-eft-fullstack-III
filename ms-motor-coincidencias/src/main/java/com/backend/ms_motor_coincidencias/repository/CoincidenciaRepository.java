package com.backend.ms_motor_coincidencias.repository;

import com.backend.ms_motor_coincidencias.model.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {
}
