package com.duoc.seguridadcalidad.security;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.Dtos.LoginResponse;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/** Autentica contra la API de login del backend y guarda el token JWT que devuelve. */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final BackendClient backend;
    private final TokenStore tokenStore;

    public CustomAuthenticationProvider(BackendClient backend, TokenStore tokenStore) {
        this.backend = backend;
        this.tokenStore = tokenStore;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        System.out.println("Login: " + name + " / " + password);

        LoginResponse respuesta = backend.login(name, password)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        tokenStore.setToken(respuesta.token());
        System.out.println("Token: " + tokenStore.getToken());

        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + respuesta.rol()));
        return new UsernamePasswordAuthenticationToken(respuesta.username(), password, autoridades);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
