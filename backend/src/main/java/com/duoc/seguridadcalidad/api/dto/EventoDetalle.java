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
        List<Foto> fotos) {

    public record Foto(String url, String descripcion) {
    }

    public static EventoDetalle de(Evento e) {
        List<Foto> fotos = e.getFotos().stream().map(f -> new Foto(f.getUrl(), f.getDescripcion())).toList();
        return new EventoDetalle(e.getId(), e.getNombre(), e.getVideojuego(), e.getCategoria(), e.getModalidad(),
                e.getPais(), e.getCiudad(), e.getFechaHora(), e.getUbicacion(), e.getEnlaceAcceso(),
                e.getOrganizador(), e.getDescripcion(), e.getRequisitos(), e.getInscripcion(), e.getCupos(),
                e.getPopularidad(), fotos);
    }
}
