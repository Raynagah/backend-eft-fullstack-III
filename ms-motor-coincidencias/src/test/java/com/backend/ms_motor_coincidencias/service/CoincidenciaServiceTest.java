package com.backend.ms_motor_coincidencias.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import com.backend.ms_motor_coincidencias.exception.BadRequestException;

/**
 * Función: CoincidenciaServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Algoritmo de Coincidencias
 * Descripción: Verifica la precisión del motor de emparejamiento. Valida el sistema de 
 * pesos porcentuales (raza, tamaño, color), los filtros excluyentes (especie), 
 * el manejo correcto de strings (mayúsculas, espacios, nulos) y el ordenamiento de resultados.
 */
class CoincidenciaServiceTest {

    private CoincidenciaService coincidenciaService;
    private ResultadoMatchDTO mascotaPerdida;

    @BeforeEach
    void setUp() {
        coincidenciaService = new CoincidenciaService();

        mascotaPerdida = new ResultadoMatchDTO();
        mascotaPerdida.setEspecie("Perro");
        mascotaPerdida.setRaza("Labrador");
        mascotaPerdida.setTamano("Grande");
        mascotaPerdida.setColor("Blanco con manchas");
    }

    /**
     * Función: evaluarCoincidencias_ParametrosNulos_LanzaBadRequestException
     * Título: Validar parámetros de entrada nulos
     * Descripción: Asegura que el servicio rechace la ejecución y arroje una BadRequestException 
     * si el reporte base o la lista de candidatas enviados son nulos.
     */
    @Test
    void evaluarCoincidencias_ParametrosNulos_LanzaBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, 
                () -> coincidenciaService.evaluarCoincidencias(null, List.of()));
        
        BadRequestException ex2 = assertThrows(BadRequestException.class, 
                () -> coincidenciaService.evaluarCoincidencias(mascotaPerdida, null));

        assertNotNull(ex1);
        assertNotNull(ex2);
    }

    /**
     * Función: evaluarCoincidencias_EspecieDistinta_DebeRetornarCeroPorCientoYFiltrarlo
     * Título: Filtro excluyente por especie
     * Descripción: Verifica que si la especie no coincide (ej. Perro vs Gato), el porcentaje 
     * se asigne a 0.0 y la candidata sea completamente excluida de la lista final.
     */
    @Test
    void evaluarCoincidencias_EspecieDistinta_DebeRetornarCeroPorCientoYFiltrarlo() {
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Gato");
        candidata.setRaza("Labrador");
        candidata.setTamano("Grande");
        candidata.setColor("Blanco con manchas");

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));
        
        assertTrue(resultados.isEmpty());
    }

    /**
     * Función: evaluarCoincidencias_MatchPerfecto_DebeRetornarCienPorCiento
     * Título: Calcular coincidencia exacta
     * Descripción: Comprueba que cuando todos los atributos son equivalentes, el algoritmo 
     * otorgue un 100% de similitud, ignorando diferencias de mayúsculas y espacios.
     */
    @Test
    void evaluarCoincidencias_MatchPerfecto_DebeRetornarCienPorCiento() {
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("perro "); 
        candidata.setRaza(" labrador");
        candidata.setTamano("GRANDE");
        candidata.setColor("blanco con manchas");

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        assertEquals(1, resultados.size());
        assertEquals(100.0, resultados.get(0).getPorcentajeSimilitud());
    }

    /**
     * Función: evaluarCoincidencias_MatchParcial_DebeSumarPesosCorrectamente
     * Título: Calcular coincidencia parcial (Candidato contenido en Original)
     * Descripción: Valida que el sistema asigne el puntaje correcto si solo algunos campos coinciden, 
     * incluyendo la validación parcial de strings ("Blanco" contenido en "Blanco con manchas").
     */
    @Test
    void evaluarCoincidencias_MatchParcial_DebeSumarPesosCorrectamente() {
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro");
        candidata.setRaza("Labrador");
        candidata.setTamano("Pequeño");
        candidata.setColor("Blanco"); 

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        assertEquals(1, resultados.size());
        assertEquals(70.0, resultados.get(0).getPorcentajeSimilitud());
    }

    /**
     * Función: evaluarCoincidencias_MatchParcialInverso_DebeSumarPesosCorrectamente
     * Título: Calcular coincidencia parcial (Original contenido en Candidato)
     * Descripción: Verifica el flujo inverso de la validación parcial de cadenas de texto 
     * para asegurar que la similitud se reconozca en ambas direcciones.
     */
    @Test
    void evaluarCoincidencias_MatchParcialInverso_DebeSumarPesosCorrectamente() {
        mascotaPerdida.setColor("Blanco");
        
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro");
        candidata.setRaza("Pug"); 
        candidata.setTamano("Pequeño"); 
        candidata.setColor("Blanco con manchas negras"); 

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        assertEquals(1, resultados.size());
        assertEquals(40.0, resultados.get(0).getPorcentajeSimilitud());
    }

    /**
     * Función: evaluarCoincidencias_DebeOrdenarDeMayorAMenor
     * Título: Ordenamiento descendente de resultados
     * Descripción: Asegura que la lista resultante se devuelva correctamente ordenada, 
     * colocando en primer lugar los prospectos con mayor porcentaje de similitud.
     */
    @Test
    void evaluarCoincidencias_DebeOrdenarDeMayorAMenor() {
        ResultadoMatchDTO baja = new ResultadoMatchDTO();
        baja.setEspecie("Perro");
        baja.setRaza("Pug");
        baja.setTamano("Pequeño");
        baja.setColor("Manchas");

        ResultadoMatchDTO alta = new ResultadoMatchDTO();
        alta.setEspecie("Perro");
        alta.setRaza("Labrador");
        alta.setTamano("Grande");
        alta.setColor("Negro");

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(baja, alta));

        assertEquals(2, resultados.size());
        assertEquals(60.0, resultados.get(0).getPorcentajeSimilitud());
        assertEquals(40.0, resultados.get(1).getPorcentajeSimilitud());
    }

    /**
     * Función: evaluarCoincidencias_ValoresNulos_NoDebenRomperElCodigo
     * Título: Tolerancia a campos nulos en candidatas
     * Descripción: Comprueba que el algoritmo maneje correctamente los objetos DTO que 
     * no tienen toda su información mapeada (atributos en null).
     */
    @Test
    void evaluarCoincidencias_ValoresNulos_NoDebenRomperElCodigo() {
        ResultadoMatchDTO candidataNula = new ResultadoMatchDTO(); 

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidataNula));
        
        assertTrue(resultados.isEmpty());
    }

    /**
     * Función: evaluarCoincidencias_CoberturaRamasNulasDeStrings
     * Título: Cobertura profunda de nulos en cadenas
     * Descripción: Valida explícitamente los escenarios donde ya sea el reporte original 
     * o la candidata presentan campos de texto nulos que podrían fallar al llamar métodos de String.
     */
    @Test
    void evaluarCoincidencias_CoberturaRamasNulasDeStrings() {
        mascotaPerdida.setEspecie(null); 
        mascotaPerdida.setColor(null);
        
        ResultadoMatchDTO candidata1 = new ResultadoMatchDTO();
        candidata1.setEspecie("Perro");
        candidata1.setColor("Blanco");

        List<ResultadoMatchDTO> res1 = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata1));
        assertTrue(res1.isEmpty()); 

        mascotaPerdida.setEspecie("Perro");
        mascotaPerdida.setColor("Blanco");
        
        ResultadoMatchDTO candidata2 = new ResultadoMatchDTO();
        candidata2.setEspecie("Perro"); 
        candidata2.setRaza(null); 
        candidata2.setTamano(null); 
        candidata2.setColor(null); 

        List<ResultadoMatchDTO> res2 = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata2));
        
        assertTrue(res2.isEmpty()); 
    }
}