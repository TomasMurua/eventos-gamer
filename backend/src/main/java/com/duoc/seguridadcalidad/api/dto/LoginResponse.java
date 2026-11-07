package com.duoc.seguridadcalidad.api.dto;

public record LoginResponse(String token, String tipo, long expiraEnSegundos, String username, String nombre, String rol) {
}
