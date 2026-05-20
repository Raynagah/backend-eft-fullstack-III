package com.backend.ms_motor_coincidencias.service;

import com.backend.ms_motor_coincidencias.dto.ResultadoMatchDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoincidenciaServiceTest {

    // Instancio mi servicio real porque aquí no dependo de base de datos ni clientes externos
    private CoincidenciaService coincidenciaService;

    private ResultadoMatchDTO mascotaPerdida;

    @BeforeEach
    void setUp() {
        coincidenciaService = new CoincidenciaService();

        // Preparo mi "Mascota Cero", la que estoy buscando
        mascotaPerdida = new ResultadoMatchDTO();
        mascotaPerdida.setEspecie("Perro");
        mascotaPerdida.setRaza("Labrador");
        mascotaPerdida.setTamano("Grande");
        mascotaPerdida.setColor("Blanco con manchas");
    }

    @Test
    void evaluarCoincidencias_EspecieDistinta_DebeRetornarCeroPorCientoYFiltrarlo() {
        // PREPARACIÓN: Un gato jamás será el perro que busco
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Gato");
        candidata.setRaza("Labrador"); // Incluso si por error el usuario puso esto
        candidata.setTamano("Grande");
        candidata.setColor("Blanco con manchas");

        // ACCIÓN
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        // VERIFICACIÓN: Como dio 0%, mi lógica lo filtró y la lista viene vacía
        assertTrue(resultados.isEmpty());
    }

    @Test
    void evaluarCoincidencias_MatchPerfecto_DebeRetornarCienPorCiento() {
        // PREPARACIÓN: Una mascota idéntica
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("perro "); // Juego con espacios y minúsculas para probar mis trim() e ignoreCase()
        candidata.setRaza(" labrador");
        candidata.setTamano("GRANDE");
        candidata.setColor("blanco con manchas");

        // ACCIÓN
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        // VERIFICACIÓN
        assertEquals(1, resultados.size());
        assertEquals(100.0, resultados.get(0).getPorcentajeSimilitud());
    }

    @Test
    void evaluarCoincidencias_MatchParcial_DebeSumarPesosCorrectamente() {
        // PREPARACIÓN: Coincide especie (base), raza (+30%), falla tamaño (+0%), color parcial (+40%) = 70%
        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro");
        candidata.setRaza("Labrador");
        candidata.setTamano("Pequeño"); // Diferente
        candidata.setColor("Blanco");   // Parcial (contenido en "Blanco con manchas")

        // ACCIÓN
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        // VERIFICACIÓN
        assertEquals(1, resultados.size());
        assertEquals(70.0, resultados.get(0).getPorcentajeSimilitud());
    }

    @Test
    void evaluarCoincidencias_DebeOrdenarDeMayorAMenor() {
        // PREPARACIÓN: Tres candidatas con distintos puntajes
        ResultadoMatchDTO baja = new ResultadoMatchDTO(); // Solo color parcial = 40%
        baja.setEspecie("Perro");
        baja.setRaza("Pug");
        baja.setTamano("Pequeño");
        baja.setColor("Manchas");

        ResultadoMatchDTO alta = new ResultadoMatchDTO(); // Raza y tamaño = 60%
        alta.setEspecie("Perro");
        alta.setRaza("Labrador");
        alta.setTamano("Grande");
        alta.setColor("Negro");

        // ACCIÓN
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(baja, alta));

        // VERIFICACIÓN: El de 60% debe estar en la posición 0, y el de 40% en la 1
        assertEquals(2, resultados.size());
        assertEquals(60.0, resultados.get(0).getPorcentajeSimilitud());
        assertEquals(40.0, resultados.get(1).getPorcentajeSimilitud());
    }

    @Test
    void evaluarCoincidencias_ValoresNulos_NoDebenRomperElCodigo() {
        // PREPARACIÓN: Mando un objeto casi vacío. Mis comprobaciones "if (val1 == null)" deben atajarlo.
        ResultadoMatchDTO candidataNula = new ResultadoMatchDTO();
        // Especie nula directamente

        // ACCIÓN
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidataNula));

        // VERIFICACIÓN: Como la especie es nula, da false en "sonIguales", da 0%, se filtra y no explota
        assertTrue(resultados.isEmpty());
    }

    @Test
    void evaluarCoincidencias_ColoresNulos_DebeDarFalso() {
        // PREPARACIÓN: Para cubrir la rama donde val1 o val2 son nulos en coincidenciaParcial
        mascotaPerdida.setColor(null);

        ResultadoMatchDTO candidata = new ResultadoMatchDTO();
        candidata.setEspecie("Perro"); // Pasa filtro especie
        candidata.setRaza("Pug");
        candidata.setTamano("Pequeño");
        candidata.setColor(null); // Color nulo

        // ACCIÓN: Dará 0% en total porque raza, tamaño fallan y color es nulo
        List<ResultadoMatchDTO> resultados = coincidenciaService.evaluarCoincidencias(mascotaPerdida, List.of(candidata));

        // VERIFICACIÓN
        assertTrue(resultados.isEmpty());
    }
}