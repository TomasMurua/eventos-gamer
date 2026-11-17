package com.duoc.seguridadcalidad.api.dto;

import java.util.Map;

/** Respuesta 400 con el mensaje de cada campo invalido. No repite el valor que envio el usuario. */
public record ErrorValidacion(int status, String mensaje, Map<String, String> errores) {
}
