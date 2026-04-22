package com.backend.gestionMascotas.exception;

public class ReporteNotFoundException extends RuntimeException {
    public ReporteNotFoundException(Long id) {
        super("El reporte con ID " + id + " no existe.");
    }
}