package com.duoc.seguridadcalidad.api.dto;

import java.util.List;

/** Contenido de la pagina de inicio: eventos recientes, populares, destacados, avisos y muro de la comunidad. */
public record Portada(
        List<EventoResumen> recientes,
        List<EventoResumen> populares,
        List<EventoResumen> destacados,
        List<BannerDto> banners,
        List<CompartidoDto> compartidos) {
}
