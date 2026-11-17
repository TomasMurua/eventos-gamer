package com.duoc.seguridadcalidad.backend;

import java.util.Map;

/**
 * El backend rechazo los datos (400, 409, 413 o 429) con un mensaje pensado para el usuario.
 * Los formularios la atrapan para mostrar el mensaje junto a los campos.
 */
public class RechazoBackendException extends RuntimeException {

    private final int status;
    private final transient Map<String, String> errores;

    public RechazoBackendException(int status, String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.status = status;
        this.errores = errores == null ? Map.of() : Map.copyOf(errores);
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
