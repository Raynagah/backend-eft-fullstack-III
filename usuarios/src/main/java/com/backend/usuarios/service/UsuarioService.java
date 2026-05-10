package com.backend.usuarios.service;

import com.backend.usuarios.dto.LoginResponseDTO;
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.backend.usuarios.security.JwtUtil;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
                .build();

        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // --- METODOO DE LOGIN ACTUALIZADO ---
    public LoginResponseDTO login(String correo, String password) {

        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        String sessionId = UUID.randomUUID().toString();
        usuario.setSessionId(sessionId);
        repository.save(usuario);

        // Utilizamos JwtUtil para generar el token real
        String token = jwtUtil.generarToken(usuario.getCorreo(), sessionId);

        // Mapeamos los datos al DTO para que no viaje la contraseña
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getCorreo()
        );

        return LoginResponseDTO.builder()
                .token(token)
                .sessionId(sessionId)
                .usuario(usuarioDTO)
                .build();
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        repository.delete(usuario);
    }

    public void logout(String sessionId) {
        Usuario usuario = repository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        usuario.setSessionId(null);
        repository.save(usuario);
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

        return repository.save(usuario);
    }

    // --- NUEVO MÉTODOo PARA VALIDAR LA SESIÓN DESDE EL BFF ---
    public boolean isSesionValida(Long id, String sessionId) {
        return repository.findById(id)
                .map(u -> u.getSessionId() != null && u.getSessionId().equals(sessionId))
                .orElse(false);
    }
}