package com.backend.bff.client;

import com.backend.bff.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-usuarios", url = "${services.usuarios.url}")
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerUsuarioPorId(@PathVariable("id") Long id);
}