package com.duoc.seguridadcalidad.security;

import org.springframework.stereotype.Component;

/** Guarda el token JWT que entrega el backend al hacer login, para usarlo en las llamadas a las API privadas. */
@Component
public class TokenStore {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
