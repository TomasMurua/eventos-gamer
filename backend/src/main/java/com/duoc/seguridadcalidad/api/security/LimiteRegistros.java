package com.duoc.seguridadcalidad.api.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Limita cuantas cuentas se pueden crear desde una misma IP en una ventana de tiempo (evita registros masivos). */
@Component
public class LimiteRegistros {

    private final Map<String, Deque<Instant>> porIp = new ConcurrentHashMap<>();
    private final int maximo;
    private final Duration ventana;
    private final Clock reloj = Clock.systemUTC();

    public LimiteRegistros(@Value("${app.registro.max-por-ip:5}") int maximo,
                           @Value("${app.registro.minutos-ventana:60}") long minutos) {
        this.maximo = maximo;
        this.ventana = Duration.ofMinutes(minutos);
    }

    /** Registra el intento y devuelve false si la IP ya alcanzo el maximo dentro de la ventana. */
    public boolean permitir(String ip) {
        Instant ahora = reloj.instant();
        Deque<Instant> marcas = porIp.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (marcas) {
            while (!marcas.isEmpty() && marcas.peekFirst().isBefore(ahora.minus(ventana))) {
                marcas.pollFirst();
            }
            if (marcas.size() >= maximo) {
                return false;
            }
            marcas.addLast(ahora);
            return true;
        }
    }
}
