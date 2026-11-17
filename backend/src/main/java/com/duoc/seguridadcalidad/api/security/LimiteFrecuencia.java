package com.duoc.seguridadcalidad.api.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Cuenta acciones por clave (por ejemplo "registro:IP" o "comentario:usuario") dentro de una ventana de tiempo,
 * para frenar abusos: registros masivos, spam de comentarios o eventos. Se guarda en memoria; las entradas
 * viejas se borran solas para que el mapa no crezca sin limite.
 */
@Component
public class LimiteFrecuencia {

    /** Ninguna ventana usada en la aplicacion dura mas que esto; lo mas antiguo se puede purgar. */
    private static final Duration VENTANA_MAXIMA = Duration.ofDays(1);
    private static final int TAMANO_PARA_PURGAR = 5_000;

    private final Map<String, Deque<Instant>> marcas = new ConcurrentHashMap<>();
    private final Clock reloj;

    @Autowired
    public LimiteFrecuencia() {
        this(Clock.systemUTC());
    }

    LimiteFrecuencia(Clock reloj) {
        this.reloj = reloj;
    }

    /** true si la clave todavia no llega al maximo de acciones dentro de la ventana. No registra nada. */
    public boolean disponible(String clave, int maximo, Duration ventana) {
        Deque<Instant> lista = marcas.get(clave);
        if (lista == null) {
            return true;
        }
        Instant desde = reloj.instant().minus(ventana);
        synchronized (lista) {
            return lista.stream().filter(t -> t.isAfter(desde)).count() < maximo;
        }
    }

    /**
     * Revisa el limite y reserva un cupo en un solo paso (dentro del mismo bloqueo), asi dos peticiones
     * simultaneas no pueden pasar las dos con el ultimo cupo. Devuelve false si ya no quedan cupos.
     */
    public boolean intentar(String clave, int maximo, Duration ventana) {
        Instant ahora = reloj.instant();
        Deque<Instant> lista = marcas.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (lista) {
            // Se descartan las marcas que ya salieron de la ventana: la cola nunca supera el maximo
            Instant desde = ahora.minus(ventana);
            while (!lista.isEmpty() && !lista.peekFirst().isAfter(desde)) {
                lista.pollFirst();
            }
            if (lista.size() >= maximo) {
                return false;
            }
            lista.addLast(ahora);
        }
        if (marcas.size() > TAMANO_PARA_PURGAR) {
            purgar(ahora);
        }
        return true;
    }

    /** Devuelve el cupo reservado con intentar() cuando la accion fallo (por ejemplo, un registro rechazado). */
    public void liberar(String clave) {
        Deque<Instant> lista = marcas.get(clave);
        if (lista != null) {
            synchronized (lista) {
                lista.pollLast();
            }
        }
    }

    /** Anota una accion realizada (por ejemplo, un registro que si se completo). */
    public void registrar(String clave) {
        Instant ahora = reloj.instant();
        Deque<Instant> lista = marcas.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (lista) {
            while (!lista.isEmpty() && lista.peekFirst().isBefore(ahora.minus(VENTANA_MAXIMA))) {
                lista.pollFirst();
            }
            lista.addLast(ahora);
        }
        if (marcas.size() > TAMANO_PARA_PURGAR) {
            purgar(ahora);
        }
    }

    /** Marcas guardadas para una clave (para los tests). */
    int tamano(String clave) {
        Deque<Instant> lista = marcas.get(clave);
        if (lista == null) {
            return 0;
        }
        synchronized (lista) {
            return lista.size();
        }
    }

    private void purgar(Instant ahora) {
        Instant limite = ahora.minus(VENTANA_MAXIMA);
        marcas.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(limite);
            }
        });
    }

    /**
     * Agrupa las IPv6 por su prefijo /64 (lo que normalmente controla una misma persona), asi no se puede
     * esquivar el limite rotando direcciones dentro de su red. Las IPv4 quedan iguales.
     */
    public static String grupoIp(String ip) {
        if (ip == null || !ip.contains(":")) {
            return ip;
        }
        try {
            byte[] bytes = InetAddress.getByName(ip).getAddress();
            if (bytes.length == 16) {
                return HexFormat.of().formatHex(bytes, 0, 8) + "::/64";
            }
        } catch (UnknownHostException e) {
            // no es una IP valida: se usa el texto tal cual
        }
        return ip;
    }
}
