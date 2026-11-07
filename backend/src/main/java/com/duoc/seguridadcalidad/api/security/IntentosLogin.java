package com.duoc.seguridadcalidad.api.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cuenta los logins fallidos por usuario y por IP. Al llegar al maximo, bloquea ese usuario o esa IP
 * durante un tiempo, lo que frena los ataques de fuerza bruta y de relleno de credenciales.
 */
@Component
public class IntentosLogin {

    private record Registro(int fallos, Instant bloqueadoHasta) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maxFallos;
    private final Duration bloqueo;
    private final Clock reloj;

    @Autowired
    public IntentosLogin(@Value("${app.login.max-fallos:5}") int maxFallos,
                         @Value("${app.login.minutos-bloqueo:15}") long minutosBloqueo) {
        this(maxFallos, Duration.ofMinutes(minutosBloqueo), Clock.systemUTC());
    }

    IntentosLogin(int maxFallos, Duration bloqueo, Clock reloj) {
        this.maxFallos = maxFallos;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    public boolean bloqueado(String usuario, String ip) {
        return bloqueada(claveUsuario(usuario)) || bloqueada(claveIp(ip));
    }

    public void registrarFallo(String usuario, String ip) {
        sumar(claveUsuario(usuario));
        sumar(claveIp(ip));
    }

    public void registrarExito(String usuario, String ip) {
        registros.remove(claveUsuario(usuario));
        registros.remove(claveIp(ip));
    }

    private boolean bloqueada(String clave) {
        Registro r = registros.get(clave);
        if (r == null || r.bloqueadoHasta() == null) {
            return false;
        }
        if (reloj.instant().isAfter(r.bloqueadoHasta())) {
            registros.remove(clave);
            return false;
        }
        return true;
    }

    private void sumar(String clave) {
        registros.compute(clave, (k, r) -> {
            int fallos = (r == null ? 0 : r.fallos()) + 1;
            Instant hasta = fallos >= maxFallos ? reloj.instant().plus(bloqueo) : null;
            return new Registro(fallos, hasta);
        });
    }

    private static String claveUsuario(String usuario) {
        return "u:" + (usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT));
    }

    private static String claveIp(String ip) {
        return "ip:" + ip;
    }
}
