package com.duoc.seguridadcalidad.api.security;

import com.duoc.seguridadcalidad.api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Emite y valida los tokens JWT (HS256) que usan las API privadas. */
@Service
public class JwtService {

    static final String EMISOR = "ljcs-api";

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-minutos}") long minutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /** Devuelve los claims si la firma, el emisor y la expiracion son validos. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long duracionEnSegundos() {
        return duracion.toSeconds();
    }
}
