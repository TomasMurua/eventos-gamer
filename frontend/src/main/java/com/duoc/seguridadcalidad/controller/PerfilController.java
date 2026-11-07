package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.security.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController {

    private final BackendClient backend;
    private final TokenStore tokenStore;

    public PerfilController(BackendClient backend, TokenStore tokenStore) {
        this.backend = backend;
        this.tokenStore = tokenStore;
    }

    /** Privada: datos del usuario que inicio sesion. */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        model.addAttribute("usuario", backend.yo(tokenStore.getToken()));
        return "perfil";
    }

    /** Solo ADMIN: usuarios registrados en la plataforma. */
    @GetMapping("/admin/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", backend.usuarios(tokenStore.getToken()));
        return "admin/usuarios";
    }
}
