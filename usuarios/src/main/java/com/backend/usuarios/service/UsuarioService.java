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

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // =========================================================================
    // 1. MÉTODOS PÚBLICOS
    // =========================================================================

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

    // =========================================================================
    // 2. MÉTODOS GENERALES
    // =========================================================================

    @Cacheable(value = "usuarios_lista")
    public List<UsuarioDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "usuario_detalle", key = "#id")
    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertirADTO(usuario); // 🔄 Convertimos
    }

    @CacheEvict(value = {"usuarios_lista", "usuario_detalle"}, allEntries = true)
    public void eliminarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        repository.delete(usuario);
    }

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

    public void logout(String sessionId) {
        Usuario usuario = repository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        usuario.setSessionId(null);
        repository.save(usuario);
    }

    public boolean isSesionValida(Long id, String sessionId) {
        return repository.findById(id)
                .map(u -> u.getSessionId() != null && u.getSessionId().equals(sessionId))
                .orElse(false);
    }

    // =========================================================================
    // 3. MÉTODOS EXCLUSIVOS PARA EL BFF (ADMIN)
    // =========================================================================

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

    // =========================================================================
    // METODO UTILITARIO PARA DTOs
    // =========================================================================
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