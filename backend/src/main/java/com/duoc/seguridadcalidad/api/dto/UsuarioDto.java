package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Usuario;

public record UsuarioDto(String username, String email, String nombre, String rol, String juegosInteres) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getUsername(), u.getEmail(), u.getNombre(), u.getRol(), u.getJuegosInteres());
    }
}
