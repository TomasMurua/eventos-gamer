package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.model.RegistroForm;
import com.duoc.seguridadcalidad.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registro", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroForm form, BindingResult result,
                            RedirectAttributes redirect) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "noCoincide", "Las contrasenas no coinciden");
        }
        if (!result.hasFieldErrors("correo") && usuarioService.existe(form.getCorreo())) {
            result.rejectValue("correo", "duplicado", "Ya existe una cuenta con ese correo");
        }
        if (result.hasErrors()) {
            form.setPassword(null);
            form.setConfirmarPassword(null);
            return "registro";
        }
        usuarioService.registrar(form);
        redirect.addAttribute("registrado", "");
        return "redirect:/login";
    }
}
