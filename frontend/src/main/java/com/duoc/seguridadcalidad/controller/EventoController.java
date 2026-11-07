package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.security.SesionUsuario;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /** Filtros que acepta el buscador y su largo maximo (los mismos limites que valida la API). */
    static final Map<String, Integer> LARGO_MAXIMO = new LinkedHashMap<>();

    static {
        LARGO_MAXIMO.put("nombre", 80);
        LARGO_MAXIMO.put("videojuego", 80);
        LARGO_MAXIMO.put("categoria", 40);
        LARGO_MAXIMO.put("lugar", 60);
        LARGO_MAXIMO.put("desde", 10);
        LARGO_MAXIMO.put("hasta", 10);
        LARGO_MAXIMO.put("modalidad", 10);
    }

    private static final Set<String> MODALIDADES = Set.of("PRESENCIAL", "ONLINE");

    private final BackendClient backend;

    public EventoController(BackendClient backend) {
        this.backend = backend;
    }

    /** Publica: busqueda y filtro de eventos. Los filtros se validan antes de consultar la API. */
    @GetMapping("/buscar")
    public String buscar(@RequestParam Map<String, String> params, Model model, HttpServletResponse response) {
        Map<String, String> filtros = new LinkedHashMap<>();
        List<String> errores = new ArrayList<>();
        LARGO_MAXIMO.forEach((filtro, maximo) -> {
            String valor = params.getOrDefault(filtro, "").trim();
            if (valor.length() > maximo) {
                errores.add("El filtro " + filtro + " admite hasta " + maximo + " caracteres.");
                valor = "";
            } else if (!valor.isEmpty() && (filtro.equals("desde") || filtro.equals("hasta")) && !esFecha(valor)) {
                errores.add("La fecha " + filtro + " no es válida.");
                valor = "";
            } else if (!valor.isEmpty() && filtro.equals("modalidad") && !MODALIDADES.contains(valor)) {
                errores.add("La modalidad debe ser presencial u online.");
                valor = "";
            }
            filtros.put(filtro, valor);
        });

        model.addAttribute("filtros", filtros);
        model.addAttribute("opciones", backend.filtros());
        model.addAttribute("errores", errores);
        if (errores.isEmpty()) {
            model.addAttribute("eventos", backend.buscar(filtros));
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("eventos", List.of());
        }
        return "eventos/buscar";
    }

    /** Privada: detalle del evento, pedido al backend con el token JWT de la sesion del usuario. */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal SesionUsuario sesion, Model model) {
        model.addAttribute("evento", backend.detalle(id, sesion.token())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado")));
        return "eventos/detalle";
    }

    private static boolean esFecha(String valor) {
        try {
            LocalDate.parse(valor);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
