package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.RechazoBackendException;
import com.duoc.seguridadcalidad.controller.form.RegistroForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/** Publica: creacion de cuentas. La cuenta queda con rol USER y la contrasena solo como hash en la API. */
@Controller
public class RegistroController {

    private static final Set<String> CAMPOS = Set.of("nombre", "username", "email", "password");

    private final BackendClient backend;

    public RegistroController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/registro")
    public String formulario(Principal principal, Model model) {
        if (principal != null) {
            return "redirect:/eventos";
        }
        model.addAttribute("form", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult resultado,
                            HttpServletRequest request, Model model) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmacion())) {
            resultado.rejectValue("confirmacion", "distinta", "Las contraseñas no coinciden");
        }
        if (!resultado.hasErrors()) {
            try {
                backend.registrar(form.paraApi(), request.getRemoteAddr());
                return "redirect:/login?registrado";
            } catch (RechazoBackendException e) {
                e.getErrores().forEach((campo, mensaje) -> {
                    if (CAMPOS.contains(campo)) {
                        resultado.rejectValue(campo, "api", mensaje);
                    }
                });
                model.addAttribute("errorGeneral", e.getMessage());
            }
        }
        // Las contrasenas nunca se devuelven en el HTML
        form.setPassword(null);
        form.setConfirmacion(null);
        return "registro";
    }
}
