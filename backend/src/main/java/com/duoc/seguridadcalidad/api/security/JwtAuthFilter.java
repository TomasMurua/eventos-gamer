package com.duoc.seguridadcalidad.api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el header "Authorization: Bearer <token>" y, si el token es valido, autentica la peticion. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            jwtService.validar(header.substring(PREFIJO.length())).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Claims claims) {
        String rol = claims.get("rol", String.class);
        var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, autoridades);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
