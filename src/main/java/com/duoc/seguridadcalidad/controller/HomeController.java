package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.service.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final EventoService eventoService;

    public HomeController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping({"/", "/home", "/recetas"})
    public String home(Model model) {
        model.addAttribute("proximos", eventoService.proximos(4));
        model.addAttribute("destacados", eventoService.destacados());
        model.addAttribute("marcas", eventoService.marcas());
        return "home";
    }
}
