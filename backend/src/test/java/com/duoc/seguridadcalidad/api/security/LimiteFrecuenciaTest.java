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
