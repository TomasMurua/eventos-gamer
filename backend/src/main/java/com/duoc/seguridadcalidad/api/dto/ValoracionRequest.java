package com.duoc.seguridadcalidad.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ValoracionRequest(@Min(1) @Max(5) int puntaje) {
}
