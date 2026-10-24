package com.duoc.seguridadcalidad.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Evento(
        Long id,
        String nombre,
        String categoria,
        String descripcionCorta,
        String descripcion,
        LocalDate fecha,
        LocalTime hora,
        String ciudad,
        String direccion,
        List<String> organizadores,
        List<String> expositores,
        Servicios servicios,
        boolean destacado) {

    public record Servicios(
            boolean estacionamiento,
            String locomocionColectiva,
            boolean comida,
            boolean alojamiento) {
    }
}
