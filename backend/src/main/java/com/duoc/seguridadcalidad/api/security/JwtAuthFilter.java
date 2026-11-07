package com.duoc.seguridadcalidad.api.security;

import com.duoc.seguridadcalidad.api.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el header "Authorization: Bearer <token>". Si la firma es valida, vuelve a buscar al usuario en la
 * base de datos: solo autentica cuentas que siguen existiendo y activas, y toma el rol desde la base de
 * datos (no desde el token), asi un cambio de rol o una baja tienen efecto inmediato.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("seguridad");
    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            jwtService.validar(header.substring(PREFIJO.length())).ifPresentOrElse(
                    this::autenticar,
                    () -> log.warn("Token JWT invalido o expirado: ruta={} ip={}", request.getRequestURI(),
                            request.getRemoteAddr()));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(Claims claims) {
        usuarios.findByUsername(claims.getSubject())
                .filter(u -> u.isActivo())
                .ifPresent(u -> {
                    var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol()));
                    var auth = new UsernamePasswordAuthenticationToken(u.getUsername(), null, autoridades);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
    }
}
