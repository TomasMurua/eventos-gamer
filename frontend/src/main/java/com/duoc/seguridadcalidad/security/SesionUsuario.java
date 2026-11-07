package com.duoc.seguridadcalidad.security;

import java.io.Serial;
import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;

/**
 * Usuario autenticado, guardado en la sesion HTTP de cada persona junto con su token JWT.
 * Asi cada sesion usa solo su propio token para llamar a las API privadas del backend.
 */
public final class SesionUsuario implements AuthenticatedPrincipal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String nombre;
    private final String rol;
    private final String token;

    public SesionUsuario(String username, String nombre, String rol, String token) {
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
        this.token = token;
    }

    @Override
    public String getName() {
        return username;
    }

    public String nombre() {
        return nombre;
    }

    public String rol() {
        return rol;
    }

    public String token() {
        return token;
    }

    /** No incluye el token, para que nunca termine en un log. */
    @Override
    public String toString() {
        return username;
    }
}
