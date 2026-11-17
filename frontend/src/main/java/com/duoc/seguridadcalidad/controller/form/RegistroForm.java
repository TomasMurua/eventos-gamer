package com.duoc.seguridadcalidad.controller.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

/** Formulario de registro. Las mismas reglas se vuelven a validar en la API. */
public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre completo")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^[\\p{L} .'-]*$", message = "El nombre solo puede tener letras, espacios, puntos y guiones")
    private String nombre;

    @NotBlank(message = "Elige un nombre de usuario")
    @Size(min = 3, max = 40, message = "El usuario debe tener entre 3 y 40 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]*$", message = "Usa letras sin tilde, números, punto, guion o guion bajo")
    private String username;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "El correo no es válido")
    @Size(max = 120, message = "El correo admite hasta 120 caracteres")
    private String email;

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres")
    private String password;

    @NotBlank(message = "Repite la contraseña")
    private String confirmacion;

    public Map<String, String> paraApi() {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("nombre", nombre);
        datos.put("username", username);
        datos.put("email", email);
        datos.put("password", password);
        return datos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}
