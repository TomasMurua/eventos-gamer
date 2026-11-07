package com.duoc.seguridadcalidad.api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String videojuego;

    @Column(nullable = false)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    @Column(nullable = false)
    private String pais;

    private String ciudad;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    private String ubicacion;

    @Column(name = "enlace_acceso")
    private String enlaceAcceso;

    @Column(nullable = false)
    private String organizador;

    @Column(nullable = false)
    private String resumen;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requisitos;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String inscripcion;

    private Integer cupos;

    @Column(nullable = false)
    private int popularidad;

    @Column(nullable = false)
    private boolean destacado;

    @Column(name = "publicado_en", nullable = false)
    private LocalDateTime publicadoEn;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    @OrderBy("orden ASC")
    private List<FotoEvento> fotos = new ArrayList<>();

    protected Evento() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getVideojuego() {
        return videojuego;
    }

    public String getCategoria() {
        return categoria;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public String getPais() {
        return pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getEnlaceAcceso() {
        return enlaceAcceso;
    }

    public String getOrganizador() {
        return organizador;
    }

    public String getResumen() {
        return resumen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public String getInscripcion() {
        return inscripcion;
    }

    public Integer getCupos() {
        return cupos;
    }

    public int getPopularidad() {
        return popularidad;
    }

    public boolean isDestacado() {
        return destacado;
    }

    public LocalDateTime getPublicadoEn() {
        return publicadoEn;
    }

    public List<FotoEvento> getFotos() {
        return fotos;
    }
}
