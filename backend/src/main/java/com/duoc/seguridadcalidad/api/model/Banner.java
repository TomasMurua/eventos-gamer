package com.duoc.seguridadcalidad.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "banners")
public class Banner {

    @Id
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private String rubro;

    @Column(nullable = false)
    private boolean activo;

    protected Banner() {
    }

    public String getMarca() {
        return marca;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRubro() {
        return rubro;
    }

    public boolean isActivo() {
        return activo;
    }
}
