package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import java.time.LocalDateTime;
import java.util.List;

public record EventoDetalle(
        Long id,
        String nombre,
        String videojuego,
        String categoria,
        Modalidad modalidad,
        String pais,
        String ciudad,
        LocalDateTime fechaHora,
        String ubicacion,
        String enlaceAcceso,
        String organizador,
        String descripcion,
        String requisitos,
        String inscripcion,
        Integer cupos,
        int popularidad,
        List<Foto> fotos,
        String autor,
        boolean editable,
        List<Medio> medios,
        Comunidad comunidad) {

    public record Foto(String url, String descripcion) {
    }

    /** Foto o video subido por el autor. El archivo se pide aparte con el id. */
    public record Medio(Long id, String tipo, String descripcion) {
    }

    public record ComentarioDto(Long id, String usuario, String texto, LocalDateTime fecha, boolean eliminable) {
    }

    /** Valoraciones, comentarios y veces compartido. miValoracion es la del usuario que hace la consulta. */
    public record Comunidad(Double promedio, int totalValoraciones, Integer miValoracion,
                            List<ComentarioDto> comentarios, long compartidos) {
    }

    public static EventoDetalle de(Evento e, boolean editable, List<Medio> medios, Comunidad comunidad) {
        List<Foto> fotos = e.getFotos().stream().map(f -> new Foto(f.getUrl(), f.getDescripcion())).toList();
        String autor = e.getAutor() == null ? null : e.getAutor().getUsername();
        return new EventoDetalle(e.getId(), e.getNombre(), e.getVideojuego(), e.getCategoria(), e.getModalidad(),
                e.getPais(), e.getCiudad(), e.getFechaHora(), e.getUbicacion(), e.getEnlaceAcceso(),
                e.getOrganizador(), e.getDescripcion(), e.getRequisitos(), e.getInscripcion(), e.getCupos(),
                e.getPopularidad(), fotos, autor, editable, medios, comunidad);
    }
}
