package com.backend.bff.service;

import com.backend.bff.client.MascotasClient;
import com.backend.bff.client.UsuarioClient;
import com.backend.bff.dto.MascotaCardDTO;
import com.backend.bff.dto.MascotaBaseDTO;
import com.backend.bff.dto.UsuarioActualizacionDTO;
import com.backend.bff.dto.UsuarioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BffUsuarioServiceTest {

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private MascotasClient mascotaClient;

    @InjectMocks
    private BffUsuarioService service;

    @Test
    void obtenerUsuario_DebeRetornarObjeto() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.obtenerUsuarioPorId(1L)).thenReturn(dto);
        assertNotNull(service.obtenerUsuario(1L));
    }

    @Test
    void actualizarUsuario_DebeRetornarObjeto() {
        UsuarioActualizacionDTO act = new UsuarioActualizacionDTO();
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.actualizarUsuario(1L, act)).thenReturn(dto);
        assertNotNull(service.actualizarUsuario(1L, act));
    }

    @Test
    void obtenerReportesPorUsuario_DebeFiltrarCorrectamente() {
        // Mascota 1: Coincide con el usuario buscado
        MascotaBaseDTO m1 = new MascotaBaseDTO();
        m1.setId(100L);
        m1.setUsuarioId(1L);
        m1.setTipoReporte("PERDIDA");
        m1.setEspecie("Perro");
        m1.setRaza("Pug");

        // Mascota 2: Pertenece a otro usuario (no coincide)
        MascotaBaseDTO m2 = new MascotaBaseDTO();
        m2.setId(101L);
        m2.setUsuarioId(2L);
        m2.setTipoReporte("ENCONTRADA");
        m2.setEspecie("Gato");
        m2.setRaza("Siamés");

        // Mascota 3: Caso extremo con usuarioId NULL para cerrar todas las ramas de la lambda
        MascotaBaseDTO m3 = new MascotaBaseDTO();
        m3.setId(102L);
        m3.setUsuarioId(null);
        m3.setTipoReporte("PERDIDA");
        m3.setEspecie("Loro");

        when(mascotaClient.obtenerTodas()).thenReturn(List.of(m1, m2, m3));

        // Ejecutamos la búsqueda para el usuario 1L
        List<MascotaCardDTO> resultados = service.obtenerReportesPorUsuario(1L);

        // Verificamos que solo se seleccionó al m1
        assertEquals(1, resultados.size());
        assertEquals(100L, resultados.get(0).getId());
    }

    @Test
    void registrar_DebeRetornarObjeto() {
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioClient.registrar(dto)).thenReturn(dto);
        assertNotNull(service.registrar(dto));
    }
}