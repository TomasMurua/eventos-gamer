package com.duoc.seguridadcalidad.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnlacesCompartirTest {

    @Test
    void elNombreDelEventoNoPuedeAgregarParametros() {
        var enlaces = EnlacesCompartir.para("https://ljcs.cl/eventos/5", "Copa {&url=https://evil.example/x} en LJCS");
        var x = enlaces.stream().filter(e -> e.red().equals("X")).findFirst().orElseThrow();
        assertThat(x.url()).startsWith("https://x.com/intent/post?text=Copa%20%7B%26url%3Dhttps")
                .endsWith("&url=https%3A%2F%2Fljcs.cl%2Feventos%2F5")
                .doesNotContain("&url=https://evil");
        assertThat(enlaces).hasSize(5);
    }
}
