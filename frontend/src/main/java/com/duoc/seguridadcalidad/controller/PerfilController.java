package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.security.SesionUsuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController {

    private final BackendClient backend;

    public PerfilController(BackendClient backend) {
        this.backend = backend;
    }

    /** Privada: datos del usuario que inicio sesion. */
    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal SesionUsuario sesion, Model model) {
        model.addAttribute("usuario", backend.yo(sesion.token()));
        return "perfil";
    }

    /** Solo ADMIN: usuarios registrados en la plataforma. */
    @GetMapping("/admin/usuarios")
    public String usuarios(@AuthenticationPrincipal SesionUsuario sesion, Model model) {
        model.addAttribute("usuarios", backend.usuarios(sesion.token()));
        return "admin/usuarios";
    }
}
