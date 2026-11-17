package com.duoc.seguridadcalidad.controller;

import com.duoc.seguridadcalidad.backend.BackendClient;
import com.duoc.seguridadcalidad.backend.RechazoBackendException;
import com.duoc.seguridadcalidad.security.SesionUsuario;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Privada: fotos y videos, comentarios, valoraciones y compartir. Cada accion llama a la API con el token
 * de la sesion; la API decide si el usuario tiene permiso (por ejemplo, solo el autor sube fotos).
 */
@Controller
@RequestMapping("/eventos/{id}")
public class ComunidadController {

    private static final String AVISO = "aviso";
    private static final String ERROR = "error";

    private final BackendClient backend;

    public ComunidadController(BackendClient backend) {
        this.backend = backend;
    }

    @PostMapping("/medios")
    public String subirMedio(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo,
                             @RequestParam(defaultValue = "") String descripcion,
                             @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        if (archivo.isEmpty()) {
            redirect.addFlashAttribute(ERROR, "Selecciona una foto o un video");
        } else {
            try {
                backend.subirMedio(id, archivo, descripcion, sesion.token());
                redirect.addFlashAttribute(AVISO, "Se agregó el archivo al evento.");
            } catch (RechazoBackendException e) {
                redirect.addFlashAttribute(ERROR, e.getMessage());
            }
        }
        return volver(id, "medios");
    }

    @GetMapping("/medios/{medioId}")
    public void medio(@PathVariable Long id, @PathVariable Long medioId,
                      @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
                      @AuthenticationPrincipal SesionUsuario sesion, HttpServletResponse response) {
        backend.transmitirMedio(id, medioId, sesion.token(), range, response);
    }

    @PostMapping("/medios/{medioId}/eliminar")
    public String eliminarMedio(@PathVariable Long id, @PathVariable Long medioId,
                                @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        backend.eliminarMedio(id, medioId, sesion.token());
        redirect.addFlashAttribute(AVISO, "Se eliminó el archivo.");
        return volver(id, "medios");
    }

    @PostMapping("/comentarios")
    public String comentar(@PathVariable Long id, @RequestParam(defaultValue = "") String texto,
                           @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        if (texto.isBlank() || texto.length() > 1000) {
            redirect.addFlashAttribute(ERROR, "El comentario debe tener entre 1 y 1000 caracteres.");
            return volver(id, "comentarios");
        }
        try {
            backend.comentar(id, texto, sesion.token());
        } catch (RechazoBackendException e) {
            redirect.addFlashAttribute(ERROR, e.getMessage());
        }
        return volver(id, "comentarios");
    }

    @PostMapping("/comentarios/{comentarioId}/eliminar")
    public String eliminarComentario(@PathVariable Long id, @PathVariable Long comentarioId,
                                     @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        backend.eliminarComentario(id, comentarioId, sesion.token());
        redirect.addFlashAttribute(AVISO, "Se eliminó el comentario.");
        return volver(id, "comentarios");
    }

    @PostMapping("/valoracion")
    public String valorar(@PathVariable Long id, @RequestParam int puntaje,
                          @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        if (puntaje < 1 || puntaje > 5) {
            redirect.addFlashAttribute(ERROR, "La valoración va de 1 a 5 estrellas.");
            return volver(id, "valoracion");
        }
        backend.valorar(id, puntaje, sesion.token());
        redirect.addFlashAttribute(AVISO, "Guardamos tu valoración.");
        return volver(id, "valoracion");
    }

    @PostMapping("/compartir")
    public String compartir(@PathVariable Long id, @RequestParam(defaultValue = "") String mensaje,
                            @AuthenticationPrincipal SesionUsuario sesion, RedirectAttributes redirect) {
        if (mensaje.length() > 280) {
            redirect.addFlashAttribute(ERROR, "El mensaje admite hasta 280 caracteres.");
            return volver(id, "compartir");
        }
        try {
            backend.compartir(id, mensaje.isBlank() ? null : mensaje, sesion.token());
            redirect.addFlashAttribute(AVISO, "Compartiste el evento en el muro de LJCS.");
        } catch (RechazoBackendException e) {
            redirect.addFlashAttribute(ERROR, e.getMessage());
        }
        return volver(id, "compartir");
    }

    private static String volver(Long id, String seccion) {
        return "redirect:/eventos/" + id + "#" + seccion;
    }
}
