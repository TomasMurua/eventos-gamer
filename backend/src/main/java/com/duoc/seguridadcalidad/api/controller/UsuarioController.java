package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.UsuarioDto;
import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Privada: datos del usuario dueno del token. */
    @GetMapping("/usuarios/me")
    public UsuarioDto yo(Principal principal) {
        return usuarios.findByUsername(principal.getName())
                .map(UsuarioDto::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Solo ADMIN: listado de usuarios registrados. */
    @GetMapping("/admin/usuarios")
    public List<UsuarioDto> todos() {
        return usuarios.findAllByOrderByIdAsc().stream().map(UsuarioDto::de).toList();
    }
}
