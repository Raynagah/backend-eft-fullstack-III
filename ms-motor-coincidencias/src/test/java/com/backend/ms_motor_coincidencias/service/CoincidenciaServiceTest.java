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

class CoincidenciaServiceTest {

    private CoincidenciaService coincidenciaService;
    private ResultadoMatchDTO mascotaPerdida;

    @BeforeEach
    @SuppressWarnings("unused") 
    void setUp() {
        coincidenciaService = new CoincidenciaService();

        mascotaPerdida = new ResultadoMatchDTO();
        mascotaPerdida.setEspecie("Perro");
        mascotaPerdida.setRaza("Labrador");
        mascotaPerdida.setTamano("Grande");
        mascotaPerdida.setColor("Blanco con manchas");
    }



    @Test
    void evaluarCoincidencias_ParametrosNulos_LanzaBadRequestException() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, 
                () -> coincidenciaService.evaluarCoincidencias(null, List.of()));
        
        BadRequestException ex2 = assertThrows(BadRequestException.class, 
                () -> coincidenciaService.evaluarCoincidencias(mascotaPerdida, null));

        assertNotNull(ex1);
        assertNotNull(ex2);
    }

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

    @Test
    void evaluarCoincidencias_MatchParcial_DebeSumarPesosCorrectamente() {
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro");
        candidata.setRaza("Labrador");
        candidata.setTamano("Pequeño");
        candidata.setColor("Blanco"); // Parcial (v1 contiene a v2)

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        assertEquals(1, resultados.size());
        assertEquals(70.0, resultados.get(0).getPorcentajeSimilitud());
    }

    @Test
    void evaluarCoincidencias_MatchParcialInverso_DebeSumarPesosCorrectamente() {
        // Prueba la otra condición del contains: v2 contiene a v1
        mascotaPerdida.setColor("Blanco");
        
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro");
        candidata.setRaza("Pug"); // Falla raza
        candidata.setTamano("Pequeño"); // Falla tamaño
        candidata.setColor("Blanco con manchas negras"); // Parcial (v2 contiene a v1)

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        assertEquals(1, resultados.size());
        assertEquals(40.0, resultados.get(0).getPorcentajeSimilitud()); // Solo suma el color
    }

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

    @Test
    void evaluarCoincidencias_ValoresNulos_NoDebenRomperElCodigo() {
        ResultadoMatchDTO candidataNula = new ResultadoMatchDTO(); // Especie = null

        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidataNula));
        assertTrue(resultados.isEmpty());
    }

    @Test
    void evaluarCoincidencias_CoberturaRamasNulasDeStrings() {
        // Caso 1: val1 es nulo en las validaciones
        mascotaPerdida.setEspecie(null); 
        mascotaPerdida.setColor(null);
        
        ResultadoMatchDTO candidata1 = new ResultadoMatchDTO();
        candidata1.setEspecie("Perro");
        candidata1.setColor("Blanco");

        List<ResultadoMatchDTO> res1 = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata1));
        assertTrue(res1.isEmpty()); // Falla especie por ser nulo

        // Caso 2: val2 es nulo en las validaciones de parcialidad
        mascotaPerdida.setEspecie("Perro");
        mascotaPerdida.setColor("Blanco");
        
        ResultadoMatchDTO candidata2 = new ResultadoMatchDTO();
        candidata2.setEspecie("Perro"); // Pasa
        candidata2.setRaza(null); // Raza nula
        candidata2.setTamano(null); // Tamaño nulo
        candidata2.setColor(null); // Color nulo

        List<ResultadoMatchDTO> res2 = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata2));
        
        // Entrará a la lista porque la especie coincide (suma 0, pero como el condicional del service es "> 0.0" para guardar, no lo guarda)
        // Ojo: si en el service pides porcentaje > 0.0, este debe venir vacío porque da exactamente 0.0 al fallar lo demás.
        assertTrue(res2.isEmpty()); 
    }
}