package com.duoc.seguridadcalidad.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FormatoMedioTest {

    static byte[] mp4(String marca) {
        byte[] b = new byte[16];
        byte[] ftyp = ("    ftyp" + marca).getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(ftyp, 0, b, 0, ftyp.length);
        return b;
    }

    @Test
    void reconoceFotosYVideosPorSuFirma() {
        assertThat(FormatoMedio.detectar(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00})).contains(FormatoMedio.JPEG);
        assertThat(FormatoMedio.detectar("RIFF....WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1))).contains(FormatoMedio.WEBP);
        assertThat(FormatoMedio.detectar(mp4("isom"))).contains(FormatoMedio.MP4);
        byte[] webm = new byte[40];
        webm[0] = 0x1A; webm[1] = 0x45; webm[2] = (byte) 0xDF; webm[3] = (byte) 0xA3;
        System.arraycopy("webm".getBytes(StandardCharsets.ISO_8859_1), 0, webm, 20, 4);
        assertThat(FormatoMedio.detectar(webm)).contains(FormatoMedio.WEBM);
    }

    @Test
    void rechazaFormatosQueNoSeAceptan() {
        // QuickTime y HEIC tambien usan "ftyp", pero no se aceptan
        assertThat(FormatoMedio.detectar(mp4("qt  "))).isEmpty();
        assertThat(FormatoMedio.detectar(mp4("heic"))).isEmpty();
        // Matroska sin "webm", SVG y un archivo vacio
        assertThat(FormatoMedio.detectar(new byte[] {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0})).isEmpty();
        assertThat(FormatoMedio.detectar("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8))).isEmpty();
        assertThat(FormatoMedio.detectar(new byte[0])).isEmpty();
        assertThat(FormatoMedio.porContentType("text/html")).isEmpty();
        assertThat(FormatoMedio.porContentType("video/mp4")).contains(FormatoMedio.MP4);
    }
}
