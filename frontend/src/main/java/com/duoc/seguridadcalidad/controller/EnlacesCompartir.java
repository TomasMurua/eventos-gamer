package com.duoc.seguridadcalidad.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Enlaces para compartir un evento en redes sociales. Son enlaces simples a la pagina de compartir de cada red:
 * no se carga ningun script de terceros en el sitio, asi que la politica CSP no cambia.
 * El texto y la URL viajan como variables de plantilla, que se codifican completas: un nombre de evento con
 * llaves o con "&url=" no puede agregar parametros al enlace.
 */
final class EnlacesCompartir {

    record Enlace(String red, String url) {
    }

    private EnlacesCompartir() {
    }

    static List<Enlace> para(String urlEvento, String texto) {
        Map<String, String> valores = Map.of("texto", texto, "url", urlEvento, "ambos", texto + " " + urlEvento);
        return List.of(
                new Enlace("WhatsApp", armar("https://wa.me/?text={ambos}", valores)),
                new Enlace("X", armar("https://x.com/intent/post?text={texto}&url={url}", valores)),
                new Enlace("Facebook", armar("https://www.facebook.com/sharer/sharer.php?u={url}", valores)),
                new Enlace("LinkedIn", armar("https://www.linkedin.com/sharing/share-offsite/?url={url}", valores)),
                new Enlace("Telegram", armar("https://t.me/share/url?url={url}&text={texto}", valores)));
    }

    private static String armar(String plantilla, Map<String, String> valores) {
        return UriComponentsBuilder.fromUriString(plantilla).encode().buildAndExpand(valores).toUriString();
    }
}
