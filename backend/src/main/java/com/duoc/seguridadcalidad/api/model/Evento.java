package com.duoc.seguridadcalidad.api.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    /** Usuario que publico el evento. Los eventos cargados por el equipo de LJCS no tienen autor. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    @OrderBy("orden ASC")
    private List<FotoEvento> fotos = new ArrayList<>();

    protected Evento() {
    }

    /** Evento nuevo publicado por un usuario. Los textos llegan ya validados desde la API. */
    public Evento(String nombre, String videojuego, String categoria, Modalidad modalidad, String pais, String ciudad,
                  LocalDateTime fechaHora, String ubicacion, String enlaceAcceso, String organizador, String resumen,
                  String descripcion, String requisitos, String inscripcion, Integer cupos, Usuario autor,
                  LocalDateTime publicadoEn) {
        this.nombre = nombre;
        this.videojuego = videojuego;
        this.categoria = categoria;
        this.modalidad = modalidad;
        this.pais = pais;
        this.ciudad = ciudad;
        this.fechaHora = fechaHora;
        this.ubicacion = ubicacion;
        this.enlaceAcceso = enlaceAcceso;
        this.organizador = organizador;
        this.resumen = resumen;
        this.descripcion = descripcion;
        this.requisitos = requisitos;
        this.inscripcion = inscripcion;
        this.cupos = cupos;
        this.autor = autor;
        this.publicadoEn = publicadoEn;
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

    public Usuario getAutor() {
        return autor;
    }

    /** true si el usuario indicado publico este evento. */
    public boolean esAutor(Usuario usuario) {
        return autor != null && usuario != null && autor.getId().equals(usuario.getId());
    }

    public List<FotoEvento> getFotos() {
        return fotos;
    }
}
