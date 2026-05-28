package com.backend.notificaciones.repository;

import com.backend.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    // Método para obtener todas las notificaciones de un usuario específico, ordenadas por fecha de creación de forma descendente
    // La búsqueda se hace por ID, pero el objeto retornado traerá todos los campos (incluido el email)
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
}