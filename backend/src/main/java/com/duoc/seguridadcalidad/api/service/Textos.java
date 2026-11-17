package com.duoc.seguridadcalidad.api.service;

/** Limpieza comun de los textos que escriben los usuarios antes de guardarlos. */
final class Textos {

    private Textos() {
    }

    /** Quita espacios al inicio y al final y los caracteres de control (deja saltos de linea y tabulaciones). */
    static String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", "").trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
