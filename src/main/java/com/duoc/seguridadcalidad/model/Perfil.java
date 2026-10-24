package com.duoc.seguridadcalidad.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class Perfil {

    @NotBlank(message = "Ingresa un nombre de usuario")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Usa solo letras, numeros, punto, guion o guion bajo")
    private String nombreUsuario;

    @Pattern(regexp = "^avatar-[1-6]$", message = "Elige uno de los avatares disponibles")
    private String avatar = "avatar-1";

    private List<String> juegosInteres = new ArrayList<>();

    private boolean notificarCorreo = true;
    private boolean notificarNuevosEventos = true;
    private boolean notificarRecordatorios;

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public List<String> getJuegosInteres() { return juegosInteres; }
    public void setJuegosInteres(List<String> juegosInteres) { this.juegosInteres = juegosInteres; }
    public boolean isNotificarCorreo() { return notificarCorreo; }
    public void setNotificarCorreo(boolean notificarCorreo) { this.notificarCorreo = notificarCorreo; }
    public boolean isNotificarNuevosEventos() { return notificarNuevosEventos; }
    public void setNotificarNuevosEventos(boolean notificarNuevosEventos) { this.notificarNuevosEventos = notificarNuevosEventos; }
    public boolean isNotificarRecordatorios() { return notificarRecordatorios; }
    public void setNotificarRecordatorios(boolean notificarRecordatorios) { this.notificarRecordatorios = notificarRecordatorios; }
}
