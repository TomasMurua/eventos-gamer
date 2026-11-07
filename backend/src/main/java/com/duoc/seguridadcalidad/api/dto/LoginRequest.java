package com.duoc.seguridadcalidad.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 120) String username,
        @NotBlank @Size(max = 128) String password) {
}
