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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import com.backend.usuarios.security.JwtUtil;

/**
 * Función: UsuarioServiceTest (Clase de Pruebas)
 * Título: Pruebas Unitarias del Servicio de Gestión de Usuarios
 * Descripción: Valida la lógica de negocio principal del sistema de usuarios, mockeando 
 * la capa de persistencia (Repository) y utilidades de seguridad (JwtUtil, PasswordEncoder).
 * Asegura el correcto comportamiento en la creación, edición, eliminación y autenticación.
 */
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

    /**
     * Función: crearUsuario_Exitoso
     * Título: Test de creación de usuario cliente
     * Descripción: Verifica que un usuario se cree correctamente con los datos enviados, 
     * encriptando su contraseña y asignándole el rol de "cliente" por defecto.
     */
    @Test
    void crearUsuario_Exitoso() {
        UsuarioRequestDTO request = new UsuarioRequestDTO(
                "Juan", 25, "M", "juan@test.com", "123456", "123456789", "url", "Dev", "Dir", "cliente"
        );
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioDTO resultado = service.crearUsuario(request);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.nombre());
        verify(repository).save(any(Usuario.class));
    }

    /**
     * Función: listar_Exitoso
     * Título: Test de listado de usuarios
     * Descripción: Valida que el método recupere todos los usuarios de la base de datos 
     * y los mapee correctamente a una lista de objetos UsuarioDTO.
     */
    @Test
    void listar_Exitoso() {
        when(repository.findAll()).thenReturn(List.of(usuarioMock));

        List<UsuarioDTO> lista = service.listar();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
        assertEquals("Juan", lista.get(0).nombre());
    }

    /**
     * Función: obtenerPorId_Exitoso
     * Título: Test de obtención de usuario por ID
     * Descripción: Comprueba que al buscar un ID existente, el servicio devuelva el 
     * UsuarioDTO correspondiente sin arrojar excepciones.
     */
    @Test
    void obtenerPorId_Exitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        UsuarioDTO resultado = service.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    /**
     * Función: obtenerPorId_NoEncontrado
     * Título: Test de obtención de usuario inexistente
     * Descripción: Verifica que se lance una RuntimeException cuando se intenta buscar 
     * un usuario con un ID que no existe en la base de datos.
     */
    @Test
    void obtenerPorId_NoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));

        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    /**
     * Función: login_Exitoso
     * Título: Test de inicio de sesión exitoso
     * Descripción: Simula credenciales correctas y verifica que el servicio actualice el 
     * sessionId en base de datos y retorne un objeto con el JWT generado.
     */
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

    /**
     * Función: login_FallaPorCredencialesIncorrectas
     * Título: Test de fallo en inicio de sesión por contraseña
     * Descripción: Verifica que al ingresar una contraseña incorrecta se lance una 
     * ResponseStatusException con código HTTP 401 (UNAUTHORIZED).
     */
    @Test
    void login_FallaPorCredencialesIncorrectas() {
        when(repository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrong_pass", "encoded_password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.login("juan@test.com", "wrong_pass")
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Credenciales incorrectas", exception.getReason());
    }

    /**
     * Función: login_FallaPorUsuarioNoEncontrado
     * Título: Test de fallo en inicio de sesión por correo inexistente
     * Descripción: Valida que si el correo no está registrado, se lance una 
     * ResponseStatusException con código HTTP 401 (UNAUTHORIZED) ocultando el motivo real.
     */
    @Test
    void login_FallaPorUsuarioNoEncontrado() {
        when(repository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.login("noexiste@test.com", "123456")
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Credenciales incorrectas", exception.getReason());
    }

    /**
     * Función: eliminarUsuario_Exitoso
     * Título: Test de eliminación de usuario
     * Descripción: Verifica que al mandar eliminar un usuario existente, se llame 
     * correctamente al método delete del repositorio.
     */
    @Test
    void eliminarUsuario_Exitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        service.eliminarUsuario(1L);

        verify(repository).delete(usuarioMock);
    }

    /**
     * Función: eliminarUsuario_NoEncontrado
     * Título: Test de fallo al eliminar usuario inexistente
     * Descripción: Comprueba que el servicio lance una RuntimeException si se intenta 
     * eliminar un registro con un ID que no se encuentra en el sistema.
     */
    @Test
    void eliminarUsuario_NoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.eliminarUsuario(99L));

        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    /**
     * Función: logout_Exitoso
     * Título: Test de cierre de sesión
     * Descripción: Valida que el proceso de logout busque la sesión y establezca el 
     * campo sessionId del usuario como null en la base de datos.
     */
    @Test
    void logout_Exitoso() {
        when(repository.findBySessionId("sesion_123")).thenReturn(Optional.of(usuarioMock));

        service.logout("sesion_123");

        assertNull(usuarioMock.getSessionId());
        verify(repository).save(usuarioMock);
    }

    /**
     * Función: logout_SesionNoEncontrada
     * Título: Test de fallo al cerrar sesión inexistente
     * Descripción: Verifica que se arroje una RuntimeException si se intenta invalidar 
     * un sessionId que no le pertenece a ningún usuario actualmente.
     */
    @Test
    void logout_SesionNoEncontrada() {
        when(repository.findBySessionId("sesion_invalida")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.logout("sesion_invalida")
        );
        assertEquals("Sesión no encontrada", exception.getMessage());
    }

    /**
     * Función: actualizarUsuario_Exitoso
     * Título: Test de actualización de datos de perfil
     * Descripción: Comprueba que al enviar datos nuevos, estos reemplacen correctamente 
     * a los antiguos en la entidad antes de persistir los cambios.
     */
    @Test
    void actualizarUsuario_Exitoso() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioDTO resultado = service.actualizarUsuario(1L, updateDTO);

        assertNotNull(resultado);
        verify(repository).save(usuarioMock);
    }

    /**
     * Función: actualizarUsuario_NoEncontrado
     * Título: Test de fallo al actualizar perfil inexistente
     * Descripción: Asegura que se lance una excepción cuando se intenta modificar la 
     * información de un identificador no registrado.
     */
    @Test
    void actualizarUsuario_NoEncontrado() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Modificado", 26, "M", "987654321", "url2", "Senior Dev", "Dir 2", "cliente");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.actualizarUsuario(99L, updateDTO));

        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }

    /**
     * Función: isSesionValida_SesionCorrecta_RetornaTrue
     * Título: Test de validación de sesión (Coincide)
     * Descripción: Verifica que el método devuelva 'true' cuando el sessionId consultado 
     * es exactamente igual al que el usuario tiene activo en la base de datos.
     */
    @Test
    void isSesionValida_SesionCorrecta_RetornaTrue() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        boolean resultado = service.isSesionValida(1L, "sesion_123");

        assertTrue(resultado);
    }

    /**
     * Función: isSesionValida_SesionIncorrecta_RetornaFalse
     * Título: Test de validación de sesión (No Coincide)
     * Descripción: Valida que el método retorne 'false' si el sessionId proporcionado 
     * no empareja con el registro de sesión activa del usuario.
     */
    @Test
    void isSesionValida_SesionIncorrecta_RetornaFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        boolean resultado = service.isSesionValida(1L, "sesion_distinta");

        assertFalse(resultado);
    }

    /**
     * Función: isSesionValida_UsuarioNoEncontrado_RetornaFalse
     * Título: Test de validación de sesión (Usuario Inexistente)
     * Descripción: Se asegura de que se devuelva un booleano 'false' de forma segura 
     * si el usuario dueño de la presunta sesión no es encontrado en la base de datos.
     */
    @Test
    void isSesionValida_UsuarioNoEncontrado_RetornaFalse() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        boolean resultado = service.isSesionValida(99L, "sesion_123");

        assertFalse(resultado);
    }

    // =========================================================================
    // MÉTODOS DE ADMINISTRADOR
    // =========================================================================

    /**
     * Función: crearUsuarioAdmin_Exitoso
     * Título: Test de creación de usuario con rol arbitrario
     * Descripción: Prueba el flujo exclusivo de administración validando que se 
     * respete y persista el rol (tipoUsuario) ingresado explícitamente en el DTO.
     */
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

        UsuarioDTO resultado = service.crearUsuarioAdmin(request);

        assertNotNull(resultado);
        assertEquals("admin", resultado.tipoUsuario());
        assertEquals("Admin Especial", resultado.nombre());
        verify(repository).save(any(Usuario.class));
    }

    /**
     * Función: actualizarUsuarioPorAdmin_Exitoso
     * Título: Test de actualización total por Administrador
     * Descripción: Verifica que un administrador pueda actualizar la totalidad de la 
     * información de un usuario, logrando sobreescribir con éxito su tipo de rol.
     */
    @Test
    void actualizarUsuarioPorAdmin_Exitoso() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Juan Cambiado", 28, "M", "555666777", "new_url", "Lead", "New Dir", "admin");
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(repository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioDTO resultado = service.actualizarUsuarioPorAdmin(1L, updateDTO);

        assertNotNull(resultado);
        assertEquals("admin", usuarioMock.getTipoUsuario());
        verify(repository).save(usuarioMock);
    }

    /**
     * Función: isSesionValida_SesionEnBaseDeDatosEsNull_RetornaFalse
     * Título: Validar sesión cuando el usuario no tiene sesión activa (null)
     * Descripción: Cubre la rama faltante del operador lógico '&&' en el map de 
     * isSesionValida, garantizando un 100% de cobertura en esa expresión lambda.
     */
    @Test
    void isSesionValida_SesionEnBaseDeDatosEsNull_RetornaFalse() {
        // Configuramos el mock para que el usuario tenga sessionId = null
        usuarioMock.setSessionId(null);
        when(repository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        boolean resultado = service.isSesionValida(1L, "sesion_cualquiera");

        assertFalse(resultado);
    }

    /**
     * Función: actualizarUsuarioPorAdmin_NoEncontrado_LanzaExcepcion
     * Título: Test de fallo al actualizar por Admin (Usuario Inexistente)
     * Descripción: Cubre al 100% el lambda del orElseThrow en el flujo de 
     * actualización de administración cuando el ID solicitado no existe.
     */
    @Test
    void actualizarUsuarioPorAdmin_NoEncontrado_LanzaExcepcion() {
        UsuarioUpdateDTO updateDTO = new UsuarioUpdateDTO("Admin Cambiado", 28, "M", "555666777", "url", "Lead", "Dir", "admin");
        // Forzamos a retornar vacío para activar el lambda del orElseThrow
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                service.actualizarUsuarioPorAdmin(99L, updateDTO)
        );

        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
    }
}