package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Compartido;
import java.time.LocalDateTime;

/** Publicacion del muro de la comunidad: quien compartio que evento y con que mensaje. */
public record CompartidoDto(Long eventoId, String evento, String usuario, String mensaje, LocalDateTime fecha) {

    public static CompartidoDto de(Compartido c) {
        return new CompartidoDto(c.getEvento().getId(), c.getEvento().getNombre(), c.getUsuario().getUsername(),
                c.getMensaje(), c.getCreadoEn());
    }
}
