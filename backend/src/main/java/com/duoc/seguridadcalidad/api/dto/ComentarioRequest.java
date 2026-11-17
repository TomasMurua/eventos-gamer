package com.duoc.seguridadcalidad.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(@NotBlank @Size(max = 1000) String texto) {
}
