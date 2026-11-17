package com.duoc.seguridadcalidad.api.dto;

import java.util.List;

/** Valores disponibles para los filtros del buscador y categorias que se pueden elegir al publicar. */
public record Filtros(List<String> videojuegos, List<String> categorias, List<String> paises, List<String> ciudades,
                      List<String> categoriasPublicables) {
}
