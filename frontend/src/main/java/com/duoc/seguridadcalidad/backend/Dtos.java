package com.duoc.seguridadcalidad.backend;

import java.time.LocalDateTime;
import java.util.List;

/** Estructuras que devuelve la API del backend. */
public final class Dtos {

    private Dtos() {
    }

    public record LoginResponse(String token, String tipo, long expiraEnSegundos, String username, String nombre,
                                String rol) {
    }

    public record EventoResumen(Long id, String nombre, String videojuego, String categoria, String modalidad,
                                String pais, String ciudad, LocalDateTime fechaHora, String resumen, int popularidad,
                                boolean destacado, String portada) {

        public String lugar() {
            return ciudad == null ? pais : ciudad + ", " + pais;
        }

        public boolean online() {
            return "ONLINE".equals(modalidad);
        }
    }

    public record Foto(String url, String descripcion) {
    }

    public record EventoDetalle(Long id, String nombre, String videojuego, String categoria, String modalidad,
                                String pais, String ciudad, LocalDateTime fechaHora, String ubicacion,
                                String enlaceAcceso, String organizador, String descripcion, String requisitos,
                                String inscripcion, Integer cupos, int popularidad, List<Foto> fotos) {

        public boolean online() {
            return "ONLINE".equals(modalidad);
        }
    }

    public record Banner(String marca, String mensaje, String rubro) {
    }

    public record Portada(List<EventoResumen> recientes, List<EventoResumen> populares,
                          List<EventoResumen> destacados, List<Banner> banners) {
    }

    public record Filtros(List<String> videojuegos, List<String> categorias, List<String> paises,
                          List<String> ciudades) {
    }

    public record Usuario(String username, String email, String nombre, String rol, String juegosInteres) {
    }
}
