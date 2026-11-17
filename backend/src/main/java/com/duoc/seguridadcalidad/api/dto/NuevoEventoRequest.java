package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Modalidad;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** Datos para publicar un evento. El autor, la fecha de publicacion y la popularidad los pone el servidor. */
public record NuevoEventoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 80) String videojuego,
        @NotBlank @Size(max = 40) String categoria,
        @NotNull Modalidad modalidad,
        @NotBlank @Size(max = 40) String pais,
        @Size(max = 60) String ciudad,
        @NotNull @Future LocalDateTime fechaHora,
        @Size(max = 200) String ubicacion,
        @Size(max = 200) String enlaceAcceso,
        @NotBlank @Size(max = 120) String organizador,
        @NotBlank @Size(max = 255) String resumen,
        @NotBlank @Size(max = 4000) String descripcion,
        @NotBlank @Size(max = 2000) String requisitos,
        @NotBlank @Size(max = 2000) String inscripcion,
        @Min(1) @Max(100000) Integer cupos) {
}
