package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.security.TokenStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    /** Filtros que acepta el buscador, en el orden en que se muestran. */
    static final List<String> FILTROS = List.of("nombre", "videojuego", "categoria", "lugar", "desde", "hasta", "modalidad");

    private final BackendClient backend;
    private final TokenStore tokenStore;

    public EventoController(BackendClient backend, TokenStore tokenStore) {
        this.backend = backend;
        this.tokenStore = tokenStore;
    }

    /** Publica: busqueda y filtro de eventos. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> filtros = new LinkedHashMap<>();
        FILTROS.forEach(f -> filtros.put(f, params.getOrDefault(f, "")));
        model.addAttribute("filtros", filtros);
        model.addAttribute("opciones", backend.filtros());
        model.addAttribute("eventos", backend.buscar(filtros));
        return "eventos/buscar";
    }

    /** Privada: detalle del evento, pedido al backend con el token JWT del usuario. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("evento", backend.detalle(id, tokenStore.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado")));
        return "eventos/detalle";
    }
}
