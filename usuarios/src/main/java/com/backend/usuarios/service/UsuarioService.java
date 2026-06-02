package com.backend.usuarios.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import com.backend.usuarios.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // =========================================================================
    // 1. MÉTODOS PÚBLICOS (Registro y edición desde el perfil del cliente)
    // =========================================================================

    public Usuario crearUsuario(UsuarioRequestDTO dto) {
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
                .tipoUsuario("cliente") // 🔒 SEGURIDAD: Forzado siempre a cliente
                .build();

        return repository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());
        
        // 🔒 SEGURIDAD: NO actualizamos el rol aquí. Se queda el que ya tenía.

        return repository.save(usuario);
    }

    // =========================================================================
    // 2. MÉTODOS GENERALES (Login, Validaciones, Listados, etc.)
    // =========================================================================

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        repository.delete(usuario);
    }

    public LoginResponseDTO login(String correo, String password) {
        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        String sessionId = UUID.randomUUID().toString();
        usuario.setSessionId(sessionId);
        repository.save(usuario);

        // Generamos token inyectando el rol y demás datos
        String token = jwtUtil.generarToken(usuario.getCorreo(), sessionId, usuario.getId(), usuario.getTipoUsuario());

        UsuarioDTO usuarioDTO = new UsuarioDTO(
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

        return LoginResponseDTO.builder()
                .token(token)
                .sessionId(sessionId)
                .usuario(usuarioDTO)
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
    // 3. MÉTODOS EXCLUSIVOS PARA EL BFF (Llamados por un ADMIN autenticado)
    // =========================================================================

    public Usuario crearUsuarioAdmin(UsuarioRequestDTO dto) {
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
                .tipoUsuario(dto.tipoUsuario()) // 🔓 Permite usar el rol elegido por el admin (admin o cliente)
                .build();

        return repository.save(usuario);
    }

    public Usuario actualizarUsuarioPorAdmin(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());
        
        // 🔓 Permite modificar el rol al editar el perfil
        usuario.setTipoUsuario(dto.tipoUsuario()); 

        return repository.save(usuario);
    }
}