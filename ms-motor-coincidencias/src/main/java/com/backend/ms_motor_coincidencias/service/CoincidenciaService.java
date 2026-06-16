package com.backend.ms_motor_coincidencias.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.exception.BadRequestException;

@Service
public class CoincidenciaService {

    public List<ResultadoMatchDTO> evaluarCoincidencias(ResultadoMatchDTO perdida, List<ResultadoMatchDTO> candidatas) {

        if (perdida == null) {
            throw new BadRequestException("El objeto de mascota original no puede ser nulo.");
        }
        if (candidatas == null) {
            throw new BadRequestException("La lista de candidatas no puede ser nula.");
        }

        List<ResultadoMatchDTO> matches = new ArrayList<>();

        for (ResultadoMatchDTO candidata : candidatas) {
            // metodo que calcula el % y además arma el texto
            calcularPorcentajeYDescripcion(perdida, candidata);

            if (candidata.getPorcentajeSimilitud() > 0.0) {
                matches.add(candidata);
            }
        }

        matches.sort(Comparator.comparing(ResultadoMatchDTO::getPorcentajeSimilitud).reversed());
        return matches;
    }

    // metodo que unifica el cálculo y la generación de la descripción
    private void calcularPorcentajeYDescripcion(ResultadoMatchDTO perdida, ResultadoMatchDTO encontrada) {
        double puntaje = 0.0;
        List<String> atributosCoincidentes = new ArrayList<>();

        // 1. Especie (Filtro base)
        if (!sonIguales(perdida.getEspecie(), encontrada.getEspecie())) {
            encontrada.setPorcentajeSimilitud(0.0);
            return;
        }
        atributosCoincidentes.add("Especie");

        // 2. Raza (Vale 30%)
        if (sonIguales(perdida.getRaza(), encontrada.getRaza())) {
            puntaje += 30.0;
            atributosCoincidentes.add("Raza");
        }

        // 3. Tamaño (Vale 30%)
        if (sonIguales(perdida.getTamano(), encontrada.getTamano())) {
            puntaje += 30.0;
            atributosCoincidentes.add("Tamaño");
        }

        // 4. Color (Vale 40%)
        if (coincidenciaParcial(perdida.getColor(), encontrada.getColor())) {
            puntaje += 40.0;
            atributosCoincidentes.add("Color");
        }

        encontrada.setPorcentajeSimilitud(puntaje);

        // 💡 Asignamos la descripción si hubo coincidencias
        if (puntaje > 0.0) {
            encontrada.setDescripcionMatch("Alta coincidencia en: " + String.join(", ", atributosCoincidentes));
        }
    }

    private boolean sonIguales(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        return val1.trim().equalsIgnoreCase(val2.trim());
    }

    private boolean coincidenciaParcial(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        String v1 = val1.toLowerCase().trim();
        String v2 = val2.toLowerCase().trim();
        return v1.contains(v2) || v2.contains(v1);
    }
}