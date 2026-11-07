package com.duoc.seguridadcalidad.security;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.LoginResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Autentica contra la API de login del backend. El token JWT que devuelve queda dentro de la
 * autenticacion de la sesion de ese usuario; la contrasena no se guarda ni se escribe en los logs.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger("seguridad");

    private final BackendClient backend;

    public CustomAuthenticationProvider(BackendClient backend) {
        this.backend = backend;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String nombre = authentication.getName();
        String password = authentication.getCredentials() == null ? "" : authentication.getCredentials().toString();

        String ip = authentication.getDetails() instanceof WebAuthenticationDetails d ? d.getRemoteAddress() : null;

        LoginResponse respuesta = backend.login(nombre, password, ip).orElseThrow(() -> {
            log.warn("Login web rechazado: usuario={} ip={}", paraLog(nombre), ip);
            return new BadCredentialsException("Usuario o contraseña incorrectos");
        });

        log.info("Login web exitoso: usuario={} ip={}", respuesta.username(), ip);
        var sesion = new SesionUsuario(respuesta.username(), respuesta.nombre(), respuesta.rol(), respuesta.token());
        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + respuesta.rol()));
        return new UsernamePasswordAuthenticationToken(sesion, null, autoridades);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /** Quita saltos de linea y caracteres de control del texto que escribio el usuario y limita el largo. */
    static String paraLog(String texto) {
        String limpio = texto.replaceAll("[\\p{Cntrl}]", "_");
        return limpio.length() > 40 ? limpio.substring(0, 40) + "..." : limpio;
    }
}
