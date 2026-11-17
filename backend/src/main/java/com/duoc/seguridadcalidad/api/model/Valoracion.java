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

/** Puntaje de 1 a 5 que un usuario le da a un evento. Hay una sola valoracion por usuario y evento. */
@Entity
@Table(name = "evento_valoraciones")
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private int puntaje;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    protected Valoracion() {
    }

    public Valoracion(Evento evento, Usuario usuario, int puntaje, LocalDateTime actualizadoEn) {
        this.evento = evento;
        this.usuario = usuario;
        this.puntaje = puntaje;
        this.actualizadoEn = actualizadoEn;
    }

    public void cambiar(int nuevoPuntaje, LocalDateTime cuando) {
        this.puntaje = nuevoPuntaje;
        this.actualizadoEn = cuando;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
