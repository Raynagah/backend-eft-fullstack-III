package com.backend.ms_motor_coincidencias.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.exception.BadRequestException;

/**
 * Función: CoincidenciaService (Servicio)
 * Título: Servicio de Evaluación de Coincidencias
 * Descripción: Contiene la lógica algorítmica central del motor de emparejamiento. Se encarga de cruzar los atributos físicos de las mascotas reportadas para calcular un porcentaje de similitud preciso y generar descripciones dinámicas de los matches.
 */
@Service
public class CoincidenciaService {

    /**
     * Función: evaluarCoincidencias
     * Título: Evaluar lista de candidatas
     * Descripción: Compara un reporte base contra una lista de posibles candidatas, calculando el nivel de similitud para cada una. Filtra aquellas que no tienen coincidencia y retorna las restantes ordenadas de mayor a menor porcentaje.
     *
     * @param perdida Objeto ResultadoMatchDTO que representa el reporte original contra el cual se realiza la evaluación.
     * @param candidatas Lista de objetos ResultadoMatchDTO que contiene las posibles mascotas a evaluar.
     * @return Lista de objetos ResultadoMatchDTO con las candidatas que superaron el porcentaje de coincidencia (> 0.0), ordenadas descendentemente.
     */
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

    /**
     * Función: calcularPorcentajeYDescripcion
     * Título: Calcular porcentaje y armar descripción
     * Descripción: Evalúa los atributos clave (especie, raza, tamaño y color) de dos registros aplicando un sistema de pesos. Especie funciona como filtro excluyente; luego se suman puntajes (raza 30%, tamaño 30%, color 40%) y se genera un texto indicando dónde hubo match.
     *
     * @param perdida Objeto ResultadoMatchDTO con la información del reporte original.
     * @param encontrada Objeto ResultadoMatchDTO de la candidata, el cual será mutado para asignarle el porcentaje final y la descripción calculada.
     */
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

        // Asignamos la descripción si hubo coincidencias
        if (puntaje > 0.0) {
            encontrada.setDescripcionMatch("Alta coincidencia en: " + String.join(", ", atributosCoincidentes));
        }
    }

    /**
     * Función: sonIguales
     * Título: Comparación exacta de textos
     * Descripción: Método utilitario privado que compara dos cadenas de texto ignorando diferencias de mayúsculas/minúsculas y espacios en blanco en los extremos.
     *
     * @param val1 Primera cadena de texto a comparar.
     * @param val2 Segunda cadena de texto a comparar.
     * @return Verdadero (true) si las cadenas son idénticas bajo los criterios mencionados, falso (false) en caso contrario o si alguna es nula.
     */
    private boolean sonIguales(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        return val1.trim().equalsIgnoreCase(val2.trim());
    }

    /**
     * Función: coincidenciaParcial
     * Título: Comparación parcial de textos
     * Descripción: Método utilitario privado que verifica si el contenido de una cadena está incluido dentro de la otra (o viceversa), independientemente de mayúsculas, minúsculas o espacios iniciales/finales.
     *
     * @param val1 Primera cadena de texto a evaluar.
     * @param val2 Segunda cadena de texto a evaluar.
     * @return Verdadero (true) si existe coincidencia cruzada entre los textos, falso (false) en caso contrario o si alguna es nula.
     */
    private boolean coincidenciaParcial(String val1, String val2) {
        if (val1 == null || val2 == null) return false;
        String v1 = val1.toLowerCase().trim();
        String v2 = val2.toLowerCase().trim();
        return v1.contains(v2) || v2.contains(v1);
    }
}