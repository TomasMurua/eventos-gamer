package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.EventoResumen;
import com.duoc.seguridadcalidad.backend.RechazoBackendException;
import com.duoc.seguridadcalidad.controller.form.EventoForm;
import com.duoc.seguridadcalidad.security.SesionUsuario;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Privada: publicar eventos y ver los eventos propios. */
@Controller
@RequestMapping("/eventos")
public class PublicacionController {

    private static final Set<String> CAMPOS = Set.of("nombre", "videojuego", "categoria", "modalidad", "pais", "ciudad",
            "fechaHora", "ubicacion", "enlaceAcceso", "organizador", "resumen", "descripcion", "requisitos",
            "inscripcion", "cupos");

    private final BackendClient backend;

    public PublicacionController(BackendClient backend) {
        this.backend = backend;
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("form", new EventoForm());
        model.addAttribute("categorias", backend.filtros().categoriasPublicables());
        return "eventos/nuevo";
    }

    @PostMapping("/nuevo")
    public String publicar(@Valid @ModelAttribute("form") EventoForm form, BindingResult resultado,
                           @AuthenticationPrincipal SesionUsuario sesion, Model model, RedirectAttributes redirect) {
        if (form.online() && (form.getEnlaceAcceso() == null || !form.getEnlaceAcceso().startsWith("https://"))) {
            resultado.rejectValue("enlaceAcceso", "https", "El enlace debe empezar con https://");
        }
        if (!form.online() && (vacio(form.getCiudad()) || vacio(form.getUbicacion()))) {
            resultado.rejectValue("ubicacion", "requerida", "Un evento presencial necesita ciudad y dirección");
        }
        if (!resultado.hasErrors()) {
            try {
                EventoResumen creado = backend.publicar(form.paraApi(), sesion.token());
                redirect.addFlashAttribute("aviso", "Publicaste el evento. Ahora puedes agregarle fotos y videos.");
                return "redirect:/eventos/" + creado.id();
            } catch (RechazoBackendException e) {
                e.getErrores().forEach((campo, mensaje) -> {
                    if (CAMPOS.contains(campo)) {
                        resultado.rejectValue(campo, "api", mensaje);
                    }
                });
                model.addAttribute("errorGeneral", e.getMessage());
            }
        }
        model.addAttribute("categorias", backend.filtros().categoriasPublicables());
        return "eventos/nuevo";
    }

    @GetMapping("/mios")
    public String mios(@AuthenticationPrincipal SesionUsuario sesion, Model model) {
        model.addAttribute("eventos", backend.misEventos(sesion.token()));
        return "eventos/mios";
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
