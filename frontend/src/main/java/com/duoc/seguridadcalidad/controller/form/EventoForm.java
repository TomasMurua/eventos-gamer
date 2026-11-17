package com.duoc.seguridadcalidad.controller.form;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

/** Formulario para publicar un evento. La API valida de nuevo y agrega el autor desde el token. */
public class EventoForm {

    @NotBlank(message = "Escribe el nombre del evento")
    @Size(max = 120, message = "Hasta 120 caracteres")
    private String nombre;

    @NotBlank(message = "Indica el videojuego")
    @Size(max = 80, message = "Hasta 80 caracteres")
    private String videojuego;

    @NotBlank(message = "Elige una categoría")
    @Size(max = 40, message = "Hasta 40 caracteres")
    private String categoria;

    @NotBlank(message = "Elige la modalidad")
    @Pattern(regexp = "PRESENCIAL|ONLINE", message = "La modalidad debe ser presencial u online")
    private String modalidad = "PRESENCIAL";

    @NotBlank(message = "Indica el país")
    @Size(max = 40, message = "Hasta 40 caracteres")
    private String pais = "Chile";

    @Size(max = 60, message = "Hasta 60 caracteres")
    private String ciudad;

    @NotNull(message = "Indica la fecha y hora")
    @Future(message = "La fecha debe ser futura")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHora;

    @Size(max = 200, message = "Hasta 200 caracteres")
    private String ubicacion;

    @Size(max = 200, message = "Hasta 200 caracteres")
    private String enlaceAcceso;

    @NotBlank(message = "Indica quién organiza")
    @Size(max = 120, message = "Hasta 120 caracteres")
    private String organizador;

    @NotBlank(message = "Escribe un resumen corto")
    @Size(max = 255, message = "Hasta 255 caracteres")
    private String resumen;

    @NotBlank(message = "Describe el evento")
    @Size(max = 4000, message = "Hasta 4000 caracteres")
    private String descripcion;

    @NotBlank(message = "Indica los requisitos para participar")
    @Size(max = 2000, message = "Hasta 2000 caracteres")
    private String requisitos;

    @NotBlank(message = "Explica cómo inscribirse")
    @Size(max = 2000, message = "Hasta 2000 caracteres")
    private String inscripcion;

    @Min(value = 1, message = "Debe haber al menos 1 cupo")
    @Max(value = 100000, message = "Máximo 100.000 cupos")
    private Integer cupos;

    public boolean online() {
        return "ONLINE".equals(modalidad);
    }

    public Map<String, Object> paraApi() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("nombre", nombre);
        datos.put("videojuego", videojuego);
        datos.put("categoria", categoria);
        datos.put("modalidad", modalidad);
        datos.put("pais", pais);
        datos.put("ciudad", online() ? null : ciudad);
        datos.put("fechaHora", fechaHora == null ? null : fechaHora.toString());
        datos.put("ubicacion", online() ? null : ubicacion);
        datos.put("enlaceAcceso", online() ? enlaceAcceso : null);
        datos.put("organizador", organizador);
        datos.put("resumen", resumen);
        datos.put("descripcion", descripcion);
        datos.put("requisitos", requisitos);
        datos.put("inscripcion", inscripcion);
        datos.put("cupos", cupos);
        return datos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVideojuego() {
        return videojuego;
    }

    public void setVideojuego(String videojuego) {
        this.videojuego = videojuego;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEnlaceAcceso() {
        return enlaceAcceso;
    }

    public void setEnlaceAcceso(String enlaceAcceso) {
        this.enlaceAcceso = enlaceAcceso;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(String inscripcion) {
        this.inscripcion = inscripcion;
    }

    public Integer getCupos() {
        return cupos;
    }

    public void setCupos(Integer cupos) {
        this.cupos = cupos;
    }
}
