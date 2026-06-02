package com.backend.usuarios.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import com.backend.usuarios.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService service;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .correo("juan@test.com")
                .password("encoded_password")
                .sessionId("sesion_123")
                .tipoUsuario("cliente")
                .build();
    }

    @Test
    void crearUsuario_Exitoso() {
        // Se incluye el campo 'cliente' obligatorio al final del DTO
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir", "cliente"
        );
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = service.crearUsuario(request);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void listar_Exitoso() {
        when(repository.findAll()).thenReturn(List.of(usuarioMock));

        List<Usuario> lista = service.listar();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Juan", lista.get(0).getNombre());
    }

    @Test
    void obtenerPorId_Exitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        Usuario resultado = service.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_NoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Atrapamos la excepción en una variable para quitar el warning
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
        
        // Validamos que el mensaje sea exactamente el que programaste en tu servicio
        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void login_Exitoso() {
        when(repository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(true);
        when(jwtUtil.generarToken(anyString(), anyString(), anyLong(), anyString())).thenReturn("mock_token");
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        LoginResponseDTO response = service.login("juan@test.com", "123456");

        assertNotNull(response);
        assertEquals("mock_token", response.getToken());
        assertNotNull(response.getSessionId());
    }

    @Test
    void login_FallaPorCredencialesIncorrectas() {
        when(repository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrong_pass", "encoded_password")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.login("juan@test.com", "wrong_pass")
        );
        assertEquals("Credenciales incorrectas", exception.getMessage());
    }

    @Test
    void login_FallaPorUsuarioNoEncontrado() {
        when(repository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.login("noexiste@test.com", "123456")
        );
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void eliminarUsuario_Exitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        service.eliminarUsuario(1L);

        verify(repository).delete(usuarioMock);
    }

    @Test
    void eliminarUsuario_NoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.eliminarUsuario(99L));
        
        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void logout_Exitoso() {
        when(repository.findBySessionId("sesion_123")).thenReturn(Optional.of(usuarioMock));

        service.logout("sesion_123");

        assertNull(usuarioMock.getSessionId());
        verify(repository).save(usuarioMock);
    }

    @Test
    void logout_SesionNoEncontrada() {
        when(repository.findBySessionId("sesion_invalida")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.logout("sesion_invalida")
        );
        assertEquals("Sesión no encontrada", exception.getMessage());
    }

    @Test
    void actualizarUsuario_Exitoso() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = service.actualizarUsuario(1L, updateDTO);

        assertNotNull(resultado);
        verify(repository).save(usuarioMock);
    }

    @Test
    void actualizarUsuario_NoEncontrado() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.actualizarUsuario(99L, updateDTO));
        
        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    @Test
    void isSesionValida_SesionCorrecta_RetornaTrue() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        boolean resultado = service.isSesionValida(1L, "sesion_123");

        assertTrue(resultado);
    }

    @Test
    void isSesionValida_SesionIncorrecta_RetornaFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        boolean resultado = service.isSesionValida(1L, "sesion_distinta");

        assertFalse(resultado);
    }

    @Test
    void isSesionValida_UsuarioNoEncontrado_RetornaFalse() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        boolean resultado = service.isSesionValida(99L, "sesion_123");

        assertFalse(resultado);
    }

    // =========================================================================
    // NUEVOS TESTS: MÉTODOS DE ADMINISTRADOR
    // =========================================================================

    @Test
    void crearUsuarioAdmin_Exitoso() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Admin Especial", 30, "M", "admin.esp@test.com", "123456", "999888777", "url_admin", "Manager", "HQ", "admin"
        );
        Usuario adminMock = Usuario.builder()
                .id(2L)
                .nombre("Admin Especial")
                .correo("admin.esp@test.com")
                .password("encoded_password")
                .tipoUsuario("admin")
                .build();

        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(repository.save(any(Usuario.class))).thenReturn(adminMock);

        Usuario resultado = service.crearUsuarioAdmin(request);

        assertNotNull(resultado);
        assertEquals("admin", resultado.getTipoUsuario());
        assertEquals("Admin Especial", resultado.getNombre());
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioPorAdmin_Exitoso() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Cambiado", 28, "M", "555666777", "new_url", "Lead", "New Dir", "admin");
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = service.actualizarUsuarioPorAdmin(1L, updateDTO);

        assertNotNull(resultado);
        // Validamos que se asigne el rol que el admin seleccionó (en este caso lo promovió a 'admin')
        assertEquals("admin", usuarioMock.getTipoUsuario());
        verify(repository).save(usuarioMock);
    }
}