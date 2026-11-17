package com.duoc.seguridadcalidad.controller;

import java.util.List;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Enlaces para compartir un evento en redes sociales. Son enlaces simples a la pagina de compartir de cada red:
 * no se carga ningun script de terceros en el sitio, asi que la politica CSP no cambia.
 */
final class EnlacesCompartir {

    record Enlace(String red, String url) {
    }

    private EnlacesCompartir() {
    }

    static List<Enlace> para(String urlEvento, String texto) {
        return List.of(
                new Enlace("WhatsApp", UriComponentsBuilder.fromUriString("https://wa.me/")
                        .queryParam("text", texto + " " + urlEvento).encode().build().toUriString()),
                new Enlace("X", UriComponentsBuilder.fromUriString("https://x.com/intent/post")
                        .queryParam("text", texto).queryParam("url", urlEvento).encode().build().toUriString()),
                new Enlace("Facebook", UriComponentsBuilder.fromUriString("https://www.facebook.com/sharer/sharer.php")
                        .queryParam("u", urlEvento).encode().build().toUriString()),
                new Enlace("LinkedIn", UriComponentsBuilder.fromUriString("https://www.linkedin.com/sharing/share-offsite/")
                        .queryParam("url", urlEvento).encode().build().toUriString()),
                new Enlace("Telegram", UriComponentsBuilder.fromUriString("https://t.me/share/url")
                        .queryParam("url", urlEvento).queryParam("text", texto).encode().build().toUriString()));
    }
}
