package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.model.Perfil;
import com.duoc.seguridadcalidad.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("juegosDisponibles")
    public Object juegosDisponibles() {
        return UsuarioService.JUEGOS;
    }

    @GetMapping("/perfil")
    public String perfil(Principal principal, Model model) {
        model.addAttribute("perfil", usuarioService.perfil(principal.getName()));
        model.addAttribute("nombre", usuarioService.nombre(principal.getName()));
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardar(@Valid @ModelAttribute("perfil") Perfil perfil, BindingResult result,
                          Principal principal, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("nombre", usuarioService.nombre(principal.getName()));
            return "perfil";
        }
        usuarioService.guardarPerfil(principal.getName(), perfil);
        redirect.addAttribute("guardado", "");
        return "redirect:/perfil";
    }
}
