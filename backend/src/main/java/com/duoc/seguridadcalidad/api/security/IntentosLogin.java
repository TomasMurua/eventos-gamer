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
 * Frena los ataques de fuerza bruta contra el login con dos contadores de fallos:
 * <ul>
 *   <li>por cuenta y por IP: 5 fallos bloquean esa cuenta solo desde esa IP, asi un atacante no puede
 *       dejar sin acceso al dueno de la cuenta, que entra desde otra IP;</li>
 *   <li>por IP: 20 fallos bloquean la IP para cualquier cuenta (relleno de credenciales).</li>
 * </ul>
 * Los bloqueos duran el tiempo configurado y un login exitoso solo limpia el contador de esa cuenta.
 */
@Component
public class IntentosLogin {

    private record Registro(int fallos, Instant bloqueadoHasta) {
    }

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final int maxFallosCuenta;
    private final int maxFallosIp;
    private final Duration bloqueo;
    private final Clock reloj;

    @Autowired
    public IntentosLogin(@Value("${app.login.max-fallos:5}") int maxFallosCuenta,
                         @Value("${app.login.max-fallos-ip:20}") int maxFallosIp,
                         @Value("${app.login.minutos-bloqueo:15}") long minutosBloqueo) {
        this(maxFallosCuenta, maxFallosIp, Duration.ofMinutes(minutosBloqueo), Clock.systemUTC());
    }

    IntentosLogin(int maxFallosCuenta, int maxFallosIp, Duration bloqueo, Clock reloj) {
        this.maxFallosCuenta = maxFallosCuenta;
        this.maxFallosIp = maxFallosIp;
        this.bloqueo = bloqueo;
        this.reloj = reloj;
    }

    /** @param cuenta nombre canonico de la cuenta (el username guardado, o el texto normalizado si no existe) */
    public boolean bloqueado(String cuenta, String ip) {
        return bloqueada(claveCuenta(cuenta, ip)) || bloqueada(claveIp(ip));
    }

    public void registrarFallo(String cuenta, String ip) {
        sumar(claveCuenta(cuenta, ip), maxFallosCuenta);
        sumar(claveIp(ip), maxFallosIp);
    }

    public void registrarExito(String cuenta, String ip) {
        registros.remove(claveCuenta(cuenta, ip));
    }

    /** Normaliza lo que escribio el usuario igual que la busqueda en la base de datos (sin espacios ni mayusculas). */
    public static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
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

    private void sumar(String clave, int maximo) {
        registros.compute(clave, (k, r) -> {
            int fallos = (r == null ? 0 : r.fallos()) + 1;
            Instant hasta = fallos >= maximo ? reloj.instant().plus(bloqueo) : null;
            return new Registro(fallos, hasta);
        });
    }

    private static String claveCuenta(String cuenta, String ip) {
        return "c:" + normalizar(cuenta) + "|" + ip;
    }

    private static String claveIp(String ip) {
        return "ip:" + ip;
    }
}
