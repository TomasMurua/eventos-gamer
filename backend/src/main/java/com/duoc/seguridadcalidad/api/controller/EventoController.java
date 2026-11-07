package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.EventoDetalle;
import com.duoc.seguridadcalidad.api.dto.EventoResumen;
import com.duoc.seguridadcalidad.api.dto.Filtros;
import com.duoc.seguridadcalidad.api.dto.Portada;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import com.duoc.seguridadcalidad.api.service.EventoService;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Validated
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    /** Publica: eventos recientes, populares, destacados y avisos comerciales. */
    @GetMapping("/portada")
    public Portada portada() {
        return service.portada();
    }

    /** Publica: valores para armar los filtros del buscador. */
    @GetMapping("/eventos/filtros")
    public Filtros filtros() {
        return service.filtros();
    }

    /** Publica: busqueda de eventos con informacion resumida. */
    @GetMapping("/eventos")
    public List<EventoResumen> buscar(
            @RequestParam(required = false) @Size(max = 80) String nombre,
            @RequestParam(required = false) @Size(max = 80) String videojuego,
            @RequestParam(required = false) @Size(max = 40) String categoria,
            @RequestParam(required = false) @Size(max = 60) String lugar,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Modalidad modalidad) {
        return service.buscar(nombre, videojuego, categoria, lugar, desde, hasta, modalidad);
    }

    /** Privada (requiere JWT): detalle completo del evento. */
    @GetMapping("/eventos/{id}")
    public EventoDetalle detalle(@PathVariable Long id) {
        return service.detalle(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }
}
