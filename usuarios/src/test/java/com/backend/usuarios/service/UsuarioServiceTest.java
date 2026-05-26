package com.backend.usuarios.service;

import com.backend.usuarios.dto.*;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import com.backend.usuarios.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    // Simulamos la base de datos y las utilidades para no depender de infraestructura externa
    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    // Inyectamos nuestros mocks dentro de la lógica de negocio real
    @InjectMocks
    private UsuarioService service;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        // Datos base confiables para usar en todos los tests
        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .correo("juan@test.com")
                .password("encoded_password")
                .sessionId("sesion_123")
                .build();
    }

    @Test
    void crearUsuario_Exitoso() {
        // PREPARACIÓN
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir"
        );
        // Cuando el servicio intente encriptar "123456", le decimos que devuelva un texto falso
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        // Cuando el servicio intente guardar en BD, devolvemos nuestro usuario de prueba
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // ACCIÓN
        Usuario resultado = service.crearUsuario(request);

        // VERIFICACIÓN
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        // verify() comprueba que el método save() del repositorio realmente fue ejecutado al menos una vez
        verify(repository).save(any(Usuario.class));
    }

    @Test
    void listar_Exitoso() {
        // PREPARACIÓN: Simulamos que la base de datos devuelve una lista con nuestro usuario mock
        when(repository.findAll()).thenReturn(List.of(usuarioMock));

        // ACCIÓN: Invocamos el método del servicio
        List<Usuario> lista = service.listar();

        // VERIFICACIÓN: Validamos que la lista no venga vacía y contenga el elemento esperado
        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Juan", lista.get(0).getNombre());
    }

    @Test
    void obtenerPorId_Exitoso() {
        // PREPARACIÓN: Simulamos que la base de datos SÍ encuentra al usuario con ID 1
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // ACCIÓN: Invocamos el método de búsqueda
        Usuario resultado = service.obtenerPorId(1L);

        // VERIFICACIÓN: Aseguramos que el objeto retornado sea el correcto
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_NoEncontrado() {
        // PREPARACIÓN: Simulamos que la base de datos no encontró absolutamente nada (Optional.empty)
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN: Aseguramos que la lógica lance una excepción, cortando el flujo
        assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void login_Exitoso() {
        // PREPARACIÓN: Para hacer login exitoso, 4 cosas deben pasar:
        // 1. Encontrar el correo en BD
        when(repository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        // 2. La contraseña debe coincidir (el mock del encoder dice que "sí, coinciden")
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(true);
        // 3. Generar un JWT exitosamente
        when(jwtUtil.generarToken(anyString(), anyString(), anyLong())).thenReturn("mock_token");
        // 4. Guardar la nueva sesión en BD
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // ACCIÓN
        LoginResponseDTO response = service.login("juan@test.com", "123456");

        // VERIFICACIÓN
        assertNotNull(response);
        assertEquals("mock_token", response.getToken());
        assertNotNull(response.getSessionId()); // Comprobamos que sí se generó un ID de sesión aleatorio
    }

    @Test
    void login_FallaPorCredencialesIncorrectas() {
        // PREPARACIÓN
        when(repository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        // Aquí forzamos el error: Le decimos al encoder mockeado que las claves NO coinciden (false)
        when(passwordEncoder.matches("wrong_pass", "encoded_password")).thenReturn(false);

        // ACCIÓN Y VERIFICACIÓN
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.login("juan@test.com", "wrong_pass")
        );
        // Además de comprobar que estalla, verificamos que estalla con el mensaje exacto esperado
        assertEquals("Credenciales incorrectas", exception.getMessage());
    }

    @Test
    void login_FallaPorUsuarioNoEncontrado() {
        // PREPARACIÓN: Simulamos que el correo ingresado no existe en el sistema
        when(repository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN: Validamos que se dispare la excepción correspondiente
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.login("noexiste@test.com", "123456")
        );
        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    void eliminarUsuario_Exitoso() {
        // PREPARACIÓN: El usuario debe existir en la BD primero antes de borrarse
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // ACCIÓN: Ejecutamos el flujo de eliminación
        service.eliminarUsuario(1L);

        // VERIFICACIÓN: Validamos que el repositorio ejecutó la instrucción .delete() con nuestro usuario
        verify(repository).delete(usuarioMock);
    }

    @Test
    void eliminarUsuario_NoEncontrado() {
        // PREPARACIÓN: Simulamos que intentamos borrar un ID que no existe
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN: Verificamos que frene la operación lanzando un error
        assertThrows(RuntimeException.class, () -> service.eliminarUsuario(99L));
    }

    @Test
    void logout_Exitoso() {
        // PREPARACIÓN
        when(repository.findBySessionId("sesion_123")).thenReturn(Optional.of(usuarioMock));

        // ACCIÓN
        service.logout("sesion_123");

        // VERIFICACIÓN
        // 1. Validamos la lógica de negocio en memoria: ¿Se le borró la sesión al usuario?
        assertNull(usuarioMock.getSessionId());
        // 2. Validamos la interacción con BD: ¿Se guardó ese borrado en el repositorio?
        verify(repository).save(usuarioMock);
    }

    @Test
    void logout_SesionNoEncontrada() {
        // PREPARACIÓN: Simulamos que el token enviado en el logout no pertenece a nadie activo
        when(repository.findBySessionId("sesion_invalida")).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.logout("sesion_invalida")
        );
        assertEquals("Sesión no encontrada", exception.getMessage());
    }

    @Test
    void actualizarUsuario_Exitoso() {
        // PREPARACIÓN: Creamos los nuevos datos de actualización
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2");
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        // ACCIÓN: Actualizamos el perfil
        Usuario resultado = service.actualizarUsuario(1L, updateDTO);

        // VERIFICACIÓN: Comprobamos el guardado de los datos modificados
        assertNotNull(resultado);
        verify(repository).save(usuarioMock);
    }

    @Test
    void actualizarUsuario_NoEncontrado() {
        // PREPARACIÓN: Intentamos actualizar un perfil inexistente
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN Y VERIFICACIÓN
        assertThrows(RuntimeException.class, () -> service.actualizarUsuario(99L, updateDTO));
    }

    @Test
    void isSesionValida_SesionCorrecta_RetornaTrue() {
        // PREPARACIÓN
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // ACCIÓN
        boolean resultado = service.isSesionValida(1L, "sesion_123");

        // VERIFICACIÓN: El ID y la sesión coinciden, por lo tanto, la lógica debe decir 'true'
        assertTrue(resultado);
    }

    @Test
    void isSesionValida_SesionIncorrecta_RetornaFalse() {
        // PREPARACIÓN: El usuario existe en BD y tiene "sesion_123"...
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        // ACCIÓN: ...pero el controlador le pregunta al servicio si "sesion_distinta" es válida
        boolean resultado = service.isSesionValida(1L, "sesion_distinta");

        // VERIFICACIÓN: Como son diferentes, rechaza el acceso devolviendo 'false'
        assertFalse(resultado);
    }

    @Test
    void isSesionValida_UsuarioNoEncontrado_RetornaFalse() {
        // PREPARACIÓN: El usuario no existe en la BD
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // ACCIÓN
        boolean resultado = service.isSesionValida(99L, "sesion_123");

        // VERIFICACIÓN: Al no existir el registro, la respuesta automática es false
        assertFalse(resultado);
    }
}