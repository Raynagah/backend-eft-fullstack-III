package com.backend.usuarios.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import com.backend.usuarios.security.JwtUtil;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

/**
 * Función: UsuarioService (Servicio)
 * Título: Servicio de Gestión de Usuarios
 * Descripción: Contiene la lógica de negocio principal para la administración de usuarios, abarcando operaciones CRUD, autenticación, manejo de sesiones en base de datos, operaciones exclusivas de administrador y gestión de la caché de la aplicación.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    // 1. MÉTODOS PÚBLICOS


    /**
     * Función: crearUsuario
     * Título: Crear usuario cliente
     * Descripción: Instancia y guarda un nuevo usuario en la base de datos asignándole por defecto el rol de "cliente" y encriptando su contraseña. Invalida la caché de la lista de usuarios.
     *
     * @param dto Objeto UsuarioRequestDTO que contiene los datos del usuario a registrar.
     * @return Objeto UsuarioDTO con la información del usuario creado.
     */
    @CacheEvict(value = "usuarios_lista", allEntries = true)
    public UsuarioDTO crearUsuario(UsuarioRequestDTO dto) {
        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre())
                .edad(dto.edad())
                .genero(dto.genero())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .fotoUrl(dto.fotoUrl())
                .ocupacion(dto.ocupacion())
                .direccion(dto.direccion())
                .password(passwordEncoder.encode(dto.password()))
                .tipoUsuario("cliente")
                .build();

        return convertirADTO(repository.save(usuario));
    }

    /**
     * Función: actualizarUsuario
     * Título: Actualizar datos del usuario
     * Descripción: Modifica la información personal de un usuario existente buscándolo por su ID. Invalida tanto la caché de la lista general como la del detalle del usuario específico.
     *
     * @param id Identificador único de tipo Long del usuario a actualizar.
     * @param dto Objeto UsuarioUpdateDTO que contiene los nuevos datos personales ingresados.
     * @return Objeto UsuarioDTO con la información del usuario ya actualizada en base de datos.
     */
    @CacheEvict(value = {"usuarios_lista", "usuario_detalle"}, allEntries = true)
    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());

        return convertirADTO(repository.save(usuario));
    }

    // 2. MÉTODOS GENERALES

    /**
     * Función: listar
     * Título: Listar todos los usuarios
     * Descripción: Obtiene todos los usuarios registrados en el sistema, convirtiéndolos a formato DTO. Utiliza la caché para optimizar las consultas recurrentes.
     *
     * @return Lista de objetos UsuarioDTO con la información de cada usuario.
     */
    @Cacheable(value = "usuarios_lista")
    public List<UsuarioDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Función: obtenerPorId
     * Título: Obtener usuario por ID
     * Descripción: Busca un usuario en la base de datos a partir de su ID. El resultado se almacena en caché para acelerar consultas futuras del mismo recurso.
     *
     * @param id Identificador único de tipo Long del usuario requerido.
     * @return Objeto UsuarioDTO con los detalles del usuario encontrado.
     */
    @Cacheable(value = "usuario_detalle", key = "#id")
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertirADTO(usuario); // 🔄 Convertimos
    }

    /**
     * Función: eliminarUsuario
     * Título: Eliminar usuario
     * Descripción: Busca y elimina permanentemente un registro de usuario de la base de datos basándose en su ID, limpiando posteriormente las cachés relacionadas.
     *
     * @param id Identificador único de tipo Long del usuario a eliminar.
     */
    @CacheEvict(value = {"usuarios_lista", "usuario_detalle"}, allEntries = true)
    public void eliminarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        repository.delete(usuario);
    }

    /**
     * Función: login
     * Título: Inicio de sesión de usuario
     * Descripción: Valida el correo y la contraseña encriptada del usuario. Si las credenciales son correctas, genera un UUID como sessionId y un token JWT, persistiendo la sesión.
     *
     * @param correo Correo electrónico ingresado por el usuario.
     * @param password Contraseña en texto plano ingresada por el usuario.
     * @return Objeto LoginResponseDTO que incluye el token JWT, el sessionId generado y los datos del usuario.
     */
    public LoginResponseDTO login(String correo, String password) {
        // 1. Usamos ResponseStatusException con HttpStatus.UNAUTHORIZED (401)
        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        // 2. Misma excepción para la contraseña
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String sessionId = UUID.randomUUID().toString();
        usuario.setSessionId(sessionId);
        repository.save(usuario);

        String token = jwtUtil.generarToken(usuario.getCorreo(), sessionId, usuario.getId(), usuario.getTipoUsuario());

        return LoginResponseDTO.builder()
                .token(token)
                .sessionId(sessionId)
                .usuario(convertirADTO(usuario))
                .build();
    }

    /**
     * Función: logout
     * Título: Cerrar sesión
     * Descripción: Invalida la sesión activa de un usuario en el sistema, buscando el registro por su sessionId y estableciéndolo como nulo.
     *
     * @param sessionId Cadena de texto correspondiente al identificador de la sesión a cerrar.
     */
    public void logout(String sessionId) {
        Usuario usuario = repository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        usuario.setSessionId(null);
        repository.save(usuario);
    }

    /**
     * Función: isSesionValida
     * Título: Validar vigencia de sesión
     * Descripción: Verifica en la base de datos si el sessionId proporcionado coincide con el que tiene activo actualmente el usuario indicado.
     *
     * @param id Identificador único de tipo Long del usuario.
     * @param sessionId Identificador de sesión que se desea verificar.
     * @return Valor booleano que indica true si la sesión coincide y es válida, o false en caso contrario.
     */
    public boolean isSesionValida(Long id, String sessionId) {
        return repository.findById(id)
                .map(u -> u.getSessionId() != null && u.getSessionId().equals(sessionId))
                .orElse(false);
    }


    // 3. MÉTODOS EXCLUSIVOS PARA EL BFF (ADMIN)

    /**
     * Función: crearUsuarioAdmin
     * Título: Crear usuario por Administrador
     * Descripción: Permite registrar un nuevo usuario con la capacidad de definir explícitamente su tipo de rol (ej. "admin", "vendedor"), a diferencia del registro estándar que fuerza el rol "cliente".
     *
     * @param dto Objeto UsuarioRequestDTO con la información de registro, incluyendo el tipoUsuario (rol).
     * @return Objeto UsuarioDTO con los datos del usuario creado.
     */
    @CacheEvict(value = "usuarios_lista", allEntries = true)
    public UsuarioDTO crearUsuarioAdmin(UsuarioRequestDTO dto) {
        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre())
                .edad(dto.edad())
                .genero(dto.genero())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .fotoUrl(dto.fotoUrl())
                .ocupacion(dto.ocupacion())
                .direccion(dto.direccion())
                .password(passwordEncoder.encode(dto.password()))
                .tipoUsuario(dto.tipoUsuario())
                .build();

        return convertirADTO(repository.save(usuario)); // 🔄 Convertimos
    }

    /**
     * Función: actualizarUsuarioPorAdmin
     * Título: Actualizar usuario por Administrador
     * Descripción: Permite la modificación de los datos de un usuario por parte de un administrador, habilitando explícitamente la actualización del atributo "tipoUsuario" (rol).
     *
     * @param id Identificador único de tipo Long del usuario a modificar.
     * @param dto Objeto UsuarioUpdateDTO con la información a actualizar, incluyendo el nuevo rol.
     * @return Objeto UsuarioDTO con la información actualizada.
     */
    @CacheEvict(value = {"usuarios_lista", "usuario_detalle"}, allEntries = true)
    public UsuarioDTO actualizarUsuarioPorAdmin(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());

        usuario.setTipoUsuario(dto.tipoUsuario());

        return convertirADTO(repository.save(usuario));
    }


    // METODO UTILITARIO PARA DTOs

    /**
     * Función: convertirADTO
     * Título: Convertir Entidad a DTO
     * Descripción: Método utilitario interno encargado de mapear los atributos de una entidad Usuario a un objeto de transferencia de datos UsuarioDTO.
     *
     * @param usuario Instancia de la entidad Usuario extraída de la base de datos.
     * @return Objeto UsuarioDTO con los campos debidamente mapeados para ser expuestos externamente.
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getCorreo(),
                usuario.getEdad(),
                usuario.getGenero(),
                usuario.getDireccion(),
                usuario.getOcupacion(),
                usuario.getFotoUrl(),
                usuario.getTipoUsuario()
        );
    }
}