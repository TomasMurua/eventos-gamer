package com.duoc.seguridadcalidad.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Evento que un usuario comparte en el muro de la comunidad, con un mensaje opcional. */
@Entity
@Table(name = "evento_compartidos")
public class Compartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 280)
    private String mensaje;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected Compartido() {
    }

    public Compartido(Evento evento, Usuario usuario, String mensaje, LocalDateTime creadoEn) {
        this.evento = evento;
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.creadoEn = creadoEn;
    }

    public Evento getEvento() {
        return evento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
