package com.duoc.seguridadcalidad.api.controller;

import com.duoc.seguridadcalidad.api.dto.ComentarioRequest;
import com.duoc.seguridadcalidad.api.dto.CompartidoDto;
import com.duoc.seguridadcalidad.api.dto.CompartirRequest;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle;
import com.duoc.seguridadcalidad.api.dto.EventoDetalle.ComentarioDto;
import com.duoc.seguridadcalidad.api.dto.EventoResumen;
import com.duoc.seguridadcalidad.api.dto.Filtros;
import com.duoc.seguridadcalidad.api.dto.NuevoEventoRequest;
import com.duoc.seguridadcalidad.api.dto.Portada;
import com.duoc.seguridadcalidad.api.dto.ValoracionRequest;
import com.duoc.seguridadcalidad.api.model.MedioEvento;
import com.duoc.seguridadcalidad.api.model.Modalidad;
import com.duoc.seguridadcalidad.api.service.ComunidadService;
import com.duoc.seguridadcalidad.api.service.EventoService;
import com.duoc.seguridadcalidad.api.service.MedioService;
import com.duoc.seguridadcalidad.api.service.MedioService.ArchivoMedio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Validated
public class EventoController {

    private final EventoService service;
    private final MedioService medios;
    private final ComunidadService comunidad;

    public EventoController(EventoService service, MedioService medios, ComunidadService comunidad) {
        this.service = service;
        this.medios = medios;
        this.comunidad = comunidad;
    }

    /** Publica: eventos recientes, populares, destacados, avisos comerciales y muro de la comunidad. */
    @GetMapping("/portada")
    public Portada portada() {
        return service.portada();
    }

    /** Publica: valores para armar los filtros del buscador. */
    @GetMapping("/eventos/filtros")
    public Filtros filtros() {
        return service.filtros();
    }

    /** Publica: busqueda de eventos con informacion resumida. */
    @GetMapping("/eventos")
    public List<EventoResumen> buscar(
            @RequestParam(required = false) @Size(max = 80) String nombre,
            @RequestParam(required = false) @Size(max = 80) String videojuego,
            @RequestParam(required = false) @Size(max = 40) String categoria,
            @RequestParam(required = false) @Size(max = 60) String lugar,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Modalidad modalidad) {
        return service.buscar(nombre, videojuego, categoria, lugar, desde, hasta, modalidad);
    }

    /** Privada: publica un evento nuevo a nombre del usuario del token. */
    @PostMapping("/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoResumen publicar(@Valid @RequestBody NuevoEventoRequest request, Principal principal) {
        return service.publicar(request, principal.getName());
    }

    /** Privada: eventos publicados por el usuario del token. */
    @GetMapping("/eventos/mios")
    public List<EventoResumen> mios(Principal principal) {
        return service.publicadosPor(principal.getName());
    }

    /** Privada (requiere JWT): detalle completo del evento. */
    @GetMapping("/eventos/{id}")
    public EventoDetalle detalle(@PathVariable Long id, Principal principal) {
        return service.detalle(id, principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
    }

    /** Privada: el autor del evento (o un ADMIN) agrega una foto o un video. */
    @PostMapping(value = "/eventos/{id}/medios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EventoDetalle.Medio subirMedio(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo,
                                          @RequestParam("descripcion") String descripcion, Principal principal) {
        MedioEvento m = medios.subir(id, principal.getName(), archivo, descripcion);
        return new EventoDetalle.Medio(m.getId(), m.getTipo().name(), m.getDescripcion());
    }

    /** Privada: entrega el archivo de una foto o video del evento. */
    @GetMapping("/eventos/{id}/medios/{medioId}")
    public ResponseEntity<Resource> medio(@PathVariable Long id, @PathVariable Long medioId) {
        ArchivoMedio archivo = medios.leer(id, medioId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(archivo.nombreDescarga()).build().toString())
                // Sin cache: tras cerrar sesion o borrar el archivo no queda una copia en el navegador
                .cacheControl(CacheControl.noStore())
                .body(archivo.recurso());
    }

    /** Privada: el autor del evento (o un ADMIN) borra una foto o un video. */
    @DeleteMapping("/eventos/{id}/medios/{medioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMedio(@PathVariable Long id, @PathVariable Long medioId, Principal principal) {
        medios.eliminar(id, medioId, principal.getName());
    }

    /** Privada: comentar un evento. */
    @PostMapping("/eventos/{id}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioDto comentar(@PathVariable Long id, @Valid @RequestBody ComentarioRequest request,
                                  Principal principal) {
        return comunidad.comentar(id, principal.getName(), request.texto());
    }

    /** Privada: borrar un comentario propio (o cualquiera, si es ADMIN). */
    @DeleteMapping("/eventos/{id}/comentarios/{comentarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarComentario(@PathVariable Long id, @PathVariable Long comentarioId, Principal principal) {
        comunidad.eliminarComentario(id, comentarioId, principal.getName());
    }

    /** Privada: valorar el evento de 1 a 5 (reemplaza la valoracion anterior del usuario). */
    @PutMapping("/eventos/{id}/valoracion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void valorar(@PathVariable Long id, @Valid @RequestBody ValoracionRequest request, Principal principal) {
        try {
            comunidad.valorar(id, principal.getName(), request.puntaje());
        } catch (DataIntegrityViolationException e) {
            // Dos valoraciones simultaneas del mismo usuario: la segunda encuentra la primera y la actualiza
            comunidad.valorar(id, principal.getName(), request.puntaje());
        }
    }

    /** Privada: compartir el evento en el muro de la comunidad. */
    @PostMapping("/eventos/{id}/compartidos")
    @ResponseStatus(HttpStatus.CREATED)
    public CompartidoDto compartir(@PathVariable Long id, @Valid @RequestBody CompartirRequest request,
                                   Principal principal) {
        return comunidad.compartir(id, principal.getName(), request.mensaje());
    }
}
