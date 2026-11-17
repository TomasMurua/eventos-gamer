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
                                String inscripcion, Integer cupos, int popularidad, List<Foto> fotos,
                                String autor, boolean editable, List<Medio> medios, Comunidad comunidad) {

        public boolean online() {
            return "ONLINE".equals(modalidad);
        }
    }

    /** Foto o video que subio el autor del evento. */
    public record Medio(Long id, String tipo, String descripcion) {

        public boolean video() {
            return "VIDEO".equals(tipo);
        }
    }

    public record Comentario(Long id, String usuario, String texto, LocalDateTime fecha, boolean eliminable) {
    }

    public record Comunidad(Double promedio, int totalValoraciones, Integer miValoracion, List<Comentario> comentarios,
                            long compartidos) {
    }

    /** Publicacion del muro de la comunidad. */
    public record Compartido(Long eventoId, String evento, String usuario, String mensaje, LocalDateTime fecha) {
    }

    public record RegistroResponse(String username, String nombre) {
    }

    /** Respuesta de error de la API: mensaje general y, si hay, el mensaje de cada campo. */
    public record ErrorApi(int status, String mensaje, java.util.Map<String, String> errores) {
    }

    public record Banner(String marca, String mensaje, String rubro) {
    }

    public record Portada(List<EventoResumen> recientes, List<EventoResumen> populares,
                          List<EventoResumen> destacados, List<Banner> banners, List<Compartido> compartidos) {
    }

    public record Filtros(List<String> videojuegos, List<String> categorias, List<String> paises,
                          List<String> ciudades, List<String> categoriasPublicables) {
    }

    public record Usuario(String username, String email, String nombre, String rol, String juegosInteres) {
    }
}
