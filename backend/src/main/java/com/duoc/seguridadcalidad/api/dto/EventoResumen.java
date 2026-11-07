package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Evento;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import java.time.LocalDateTime;

/** Datos que se muestran en listados y resultados de busqueda. */
public record EventoResumen(
        Long id,
        String nombre,
        String videojuego,
        String categoria,
        Modalidad modalidad,
        String pais,
        String ciudad,
        LocalDateTime fechaHora,
        String resumen,
        int popularidad,
        boolean destacado,
        String portada) {

    public static EventoResumen de(Evento e) {
        String portada = e.getFotos().isEmpty() ? null : e.getFotos().get(0).getUrl();
        return new EventoResumen(e.getId(), e.getNombre(), e.getVideojuego(), e.getCategoria(), e.getModalidad(),
                e.getPais(), e.getCiudad(), e.getFechaHora(), e.getResumen(), e.getPopularidad(), e.isDestacado(),
                portada);
    }
}
