package com.duoc.seguridadcalidad.api.error;

/** El usuario o la IP supero un limite de frecuencia o de cuota. El mensaje se muestra al usuario. */
public class DemasiadasSolicitudesException extends RuntimeException {

    public DemasiadasSolicitudesException(String mensaje) {
        super(mensaje);
    }
}
