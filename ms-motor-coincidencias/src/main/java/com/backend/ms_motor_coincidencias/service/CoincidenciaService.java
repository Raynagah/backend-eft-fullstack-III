package com.backend.ms_motor_coincidencias.service;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CoincidenciaService {

    // Metodo principal que evalúa una mascota perdida contra una lista de encontradas
    public List<ResultadoMatchDTO> evaluarCoincidencias(ResultadoMatchDTO perdida, List<ResultadoMatchDTO> candidatas) {
        List<ResultadoMatchDTO> matches = new ArrayList<>();

        for (ResultadoMatchDTO candidata : candidatas) {
            double porcentaje = calcularPorcentaje(perdida, candidata);

            // Solo devolvemos coincidencias que tengan al menos algo en común (ej. > 0%)
            if (porcentaje > 0.0) {
                candidata.setPorcentajeSimilitud(porcentaje);
                matches.add(candidata);
            }
        }

        // Ordenamos la lista de mayor a menor porcentaje (los mejores matches primero)
        matches.sort(Comparator.comparing(ResultadoMatchDTO::getPorcentajeSimilitud).reversed());
        return matches;
    }

    // El Algoritmo Matemático de Pesos
    private double calcularPorcentaje(ResultadoMatchDTO perdida, ResultadoMatchDTO encontrada) {
        double puntaje = 0.0;

        // 1. Si la especie no es la misma, es 0% (un gato no es un perro)
        if (!sonIguales(perdida.getEspecie(), encontrada.getEspecie())) {
            return 0.0;
        }

        // Si pasamos el filtro de especie, ya partimos con una base, pero evaluaremos el resto sobre 100%

        // 2. Raza (Vale 30%)
        if (sonIguales(perdida.getRaza(), encontrada.getRaza())) {
            puntaje += 30.0;
        }

        // 3. Tamaño (Vale 30%)
        if (sonIguales(perdida.getTamano(), encontrada.getTamano())) {
            puntaje += 30.0;
        }

        // 4. Color (Vale 40% - Búsqueda más flexible)
        if (coincidenciaParcial(perdida.getColor(), encontrada.getColor())) {
            puntaje += 40.0;
        }

        return puntaje;
    }

    // Funciones de limpieza y comparación de Strings
    private boolean sonIguales(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        return val1.trim().equalsIgnoreCase(val2.trim());
    }

    private boolean coincidenciaParcial(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        String v1 = val1.toLowerCase().trim();
        String v2 = val2.toLowerCase().trim();
        // Si uno contiene la palabra del otro (ej: "Blanco" y "Blanco con negro")
        return v1.contains(v2) || v2.contains(v1);
    }
}
