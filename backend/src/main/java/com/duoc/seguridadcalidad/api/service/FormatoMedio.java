package com.duoc.seguridadcalidad.api.service;

import com.duoc.seguridadcalidad.api.model.MedioEvento.Tipo;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Formatos de foto y video aceptados. El formato se reconoce por los primeros bytes del archivo
 * (su "firma"), no por la extension ni por el Content-Type que manda el navegador, que el usuario controla.
 */
enum FormatoMedio {

    JPEG(Tipo.FOTO, "image/jpeg", "jpg"),
    PNG(Tipo.FOTO, "image/png", "png"),
    WEBP(Tipo.FOTO, "image/webp", "webp"),
    MP4(Tipo.VIDEO, "video/mp4", "mp4"),
    WEBM(Tipo.VIDEO, "video/webm", "webm");

    /** Marcas de MP4 que los navegadores reproducen (se excluyen QuickTime y HEIC, que tambien usan "ftyp"). */
    private static final Set<String> MARCAS_MP4 = Set.of("isom", "iso2", "iso4", "iso5", "iso6", "mp41", "mp42",
            "avc1", "M4V ", "dash", "MSNV");

    final Tipo tipo;
    final String contentType;
    final String extension;

    FormatoMedio(Tipo tipo, String contentType, String extension) {
        this.tipo = tipo;
        this.contentType = contentType;
        this.extension = extension;
    }

    static Optional<FormatoMedio> detectar(byte[] b) {
        if (empieza(b, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPEG);
        }
        if (empieza(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(PNG);
        }
        if (texto(b, 0, "RIFF") && texto(b, 8, "WEBP")) {
            return Optional.of(WEBP);
        }
        if (texto(b, 4, "ftyp") && b.length >= 12
                && MARCAS_MP4.contains(new String(b, 8, 4, StandardCharsets.ISO_8859_1))) {
            return Optional.of(MP4);
        }
        if (empieza(b, 0, 0x1A, 0x45, 0xDF, 0xA3) && contiene(b, "webm")) {
            return Optional.of(WEBM);
        }
        return Optional.empty();
    }

    static Optional<FormatoMedio> porContentType(String contentType) {
        return Arrays.stream(values()).filter(f -> f.contentType.equals(contentType)).findFirst();
    }

    private static boolean empieza(byte[] b, int desde, int... firma) {
        if (b.length < desde + firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((b[desde + i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean texto(byte[] b, int desde, String texto) {
        return empieza(b, desde, texto.chars().toArray());
    }

    private static boolean contiene(byte[] b, String texto) {
        return new String(b, StandardCharsets.ISO_8859_1).contains(texto);
    }
}
