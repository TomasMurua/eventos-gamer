package com.duoc.seguridadcalidad.api.dto;

import java.util.List;

/** Valores disponibles para los filtros del buscador. */
public record Filtros(List<String> videojuegos, List<String> categorias, List<String> paises, List<String> ciudades) {
}
