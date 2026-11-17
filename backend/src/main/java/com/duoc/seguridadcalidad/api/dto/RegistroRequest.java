package com.duoc.seguridadcalidad.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del formulario de registro. No trae rol: toda cuenta nueva queda como USER,
 * asi nadie puede registrarse como administrador agregando un campo extra al JSON.
 */
public record RegistroRequest(
        @NotBlank @Size(min = 2, max = 80)
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "solo puede tener letras, espacios, puntos, apóstrofos y guiones")
        String nombre,

        @NotBlank @Size(min = 3, max = 40)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "solo puede tener letras sin tilde, números, punto, guion y guion bajo")
        String username,

        @NotBlank @Email @Size(max = 120)
        String email,

        @NotBlank @Size(min = 10, max = 64)
        String password) {
}
