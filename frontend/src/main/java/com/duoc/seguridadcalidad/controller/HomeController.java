package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.Portada;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BackendClient backend;

    public HomeController(BackendClient backend) {
        this.backend = backend;
    }

    /** Pagina de inicio publica. La ruta principal del sitio es /eventos. */
    @GetMapping({"/eventos", "/", "/home", "/recetas"})
    public String home(Model model) {
        Portada portada = backend.portada();
        model.addAttribute("recientes", portada.recientes());
        model.addAttribute("populares", portada.populares());
        model.addAttribute("destacados", portada.destacados());
        model.addAttribute("banners", portada.banners());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
