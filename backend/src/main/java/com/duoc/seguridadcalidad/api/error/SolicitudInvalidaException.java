package com.duoc.seguridadcalidad.api.error;

/** Datos que pasan la validacion de formato pero no las reglas del negocio. El mensaje se muestra al usuario. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
