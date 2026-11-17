package com.duoc.seguridadcalidad.api.model;

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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Foto o video que el autor sube a su evento. El archivo vive en disco con el nombre aleatorio de "archivo". */
@Entity
@Table(name = "evento_medios")
public class MedioEvento {

    public enum Tipo {
        FOTO,
        VIDEO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Tipo tipo;

    @Column(nullable = false, unique = true, length = 64)
    private String archivo;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    @Column(nullable = false)
    private long tamano;

    @Column(nullable = false, length = 160)
    private String descripcion;

    @Column(name = "subido_en", nullable = false)
    private LocalDateTime subidoEn;

    protected MedioEvento() {
    }

    public MedioEvento(Evento evento, Usuario usuario, Tipo tipo, String archivo, String contentType, long tamano,
                       String descripcion, LocalDateTime subidoEn) {
        this.evento = evento;
        this.usuario = usuario;
        this.tipo = tipo;
        this.archivo = archivo;
        this.contentType = contentType;
        this.tamano = tamano;
        this.descripcion = descripcion;
        this.subidoEn = subidoEn;
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getArchivo() {
        return archivo;
    }

    public String getContentType() {
        return contentType;
    }

    public long getTamano() {
        return tamano;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getSubidoEn() {
        return subidoEn;
    }
}
