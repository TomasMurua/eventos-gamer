package com.duoc.seguridadcalidad.api.dto;

import jakarta.validation.constraints.Size;

public record CompartirRequest(@Size(max = 280) String mensaje) {
}
