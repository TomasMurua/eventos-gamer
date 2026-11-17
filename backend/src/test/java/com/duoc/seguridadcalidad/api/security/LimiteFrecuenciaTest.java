package com.duoc.seguridadcalidad.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LimiteFrecuenciaTest {

    @Test
    void cuentaSoloDentroDeLaVentana() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC);
        LimiteFrecuencia limites = new LimiteFrecuencia(reloj);
        limites.registrar("x");
        limites.registrar("x");
        assertThat(limites.disponible("x", 2, Duration.ofMinutes(1))).isFalse();
        assertThat(limites.disponible("x", 3, Duration.ofMinutes(1))).isTrue();
        assertThat(limites.disponible("otra", 1, Duration.ofMinutes(1))).isTrue();
    }

    @Test
    void reservaYLiberaCuposEnUnSoloPaso() throws Exception {
        LimiteFrecuencia limites = new LimiteFrecuencia();
        // 20 hilos compiten por 5 cupos: nunca pasan mas de 5
        var hilos = java.util.concurrent.Executors.newFixedThreadPool(8);
        var aceptados = new java.util.concurrent.atomic.AtomicInteger();
        var tareas = new java.util.ArrayList<java.util.concurrent.Callable<Void>>();
        for (int i = 0; i < 20; i++) {
            tareas.add(() -> {
                if (limites.intentar("r", 5, Duration.ofMinutes(1))) {
                    aceptados.incrementAndGet();
                }
                return null;
            });
        }
        hilos.invokeAll(tareas);
        hilos.shutdown();
        assertThat(aceptados.get()).isEqualTo(5);
        limites.liberar("r");
        assertThat(limites.intentar("r", 5, Duration.ofMinutes(1))).isTrue();
        assertThat(limites.intentar("r", 5, Duration.ofMinutes(1))).isFalse();
    }

    /** Reloj que la prueba puede adelantar. */
    static class RelojMovil extends Clock {
        Instant ahora = Instant.parse("2026-11-17T10:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return ahora;
        }
    }

    @Test
    void borraLasEntradasViejasParaQueElMapaNoCrezcaSinLimite() {
        RelojMovil reloj = new RelojMovil();
        LimiteFrecuencia limites = new LimiteFrecuencia(reloj);
        for (int i = 0; i <= 5_000; i++) {
            limites.registrar("ip:" + i);
        }
        reloj.ahora = reloj.ahora.plus(Duration.ofDays(2));
        limites.registrar("nueva");
        // Con una ventana de 3 dias la marca vieja contaria, pero ya se purgo
        assertThat(limites.disponible("ip:1", 1, Duration.ofDays(3))).isTrue();
        assertThat(limites.disponible("nueva", 1, Duration.ofDays(3))).isFalse();
    }

    @Test
    void laColaDeUnaClaveNoCreceConElTiempo() {
        RelojMovil reloj = new RelojMovil();
        LimiteFrecuencia limites = new LimiteFrecuencia(reloj);
        for (int minuto = 0; minuto < 600; minuto++) {
            assertThat(limites.intentar("c", 5, Duration.ofMinutes(1))).isTrue();
            reloj.ahora = reloj.ahora.plus(Duration.ofMinutes(1));
        }
        assertThat(limites.tamano("c")).isLessThanOrEqualTo(5);
    }

    @Test
    void agrupaIpv6PorPrefijo64() {
        assertThat(LimiteFrecuencia.grupoIp("2800:150:1:2::10"))
                .isEqualTo(LimiteFrecuencia.grupoIp("2800:150:1:2:ffff::99"));
        assertThat(LimiteFrecuencia.grupoIp("2800:150:1:2::10"))
                .isNotEqualTo(LimiteFrecuencia.grupoIp("2800:150:1:3::10"));
        assertThat(LimiteFrecuencia.grupoIp("190.5.1.20")).isEqualTo("190.5.1.20");
        assertThat(LimiteFrecuencia.grupoIp("zz::zz")).isEqualTo("zz::zz");
        assertThat(LimiteFrecuencia.grupoIp(null)).isNull();
    }
}
